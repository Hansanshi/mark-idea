package ink.markidea.note.service.impl;

import com.github.benmanes.caffeine.cache.LoadingCache;
import ink.markidea.note.dao.DelNoteRepository;
import ink.markidea.note.dao.DraftNoteRepository;
import ink.markidea.note.entity.DelNoteDo;
import ink.markidea.note.entity.dto.NotePreviewInfo;
import ink.markidea.note.entity.dto.UserNoteKey;
import ink.markidea.note.entity.exception.PromptException;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.DeletedNoteVo;
import ink.markidea.note.entity.vo.NoteVersionVo;
import ink.markidea.note.entity.vo.NoteVo;
import ink.markidea.note.service.IArticleService;
import ink.markidea.note.service.IFileService;
import ink.markidea.note.service.INoteService;
import ink.markidea.note.util.DateTimeUtil;
import ink.markidea.note.util.FileUtil;
import ink.markidea.note.util.GitUtil;
import ink.markidea.note.util.ThreadLocalUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hansanshi
 * @date 2019/12/28
 */
@Service
@Slf4j
public class NoteServiceImpl implements INoteService {

    @Value("${notesDir}")
    private String notesDir;

    @Autowired
    private IFileService fileService;

    @Autowired
    private DelNoteRepository delNoteRepository;

    @Autowired
    private DraftNoteRepository draftNoteRepository;

    private static final String NOTEBOOK_FLAG_FILE = ".notebook";

    @Autowired
    @Qualifier("userNoteCache")
    LoadingCache<UserNoteKey, String> userNoteCache;


    @Autowired
    @Qualifier("userNotePreviewCache")
    LoadingCache<UserNoteKey, NotePreviewInfo> userNotePreviewCache;


    @Autowired
    private IArticleService articleService;

    @Override
    public ServerResponse<List<String>> listNotebooks(){
        File dir = getOrCreateUserNotebookDir();
        File[] childFiles = dir.listFiles();
        if (childFiles == null || childFiles.length == 0){
            return ServerResponse.buildSuccessResponse(Collections.emptyList());
        }
        List<String> notebookNameList = new ArrayList<>();
        for (File file : childFiles){
            if (file.isDirectory()){
                String fileName = file.getName();
                if (fileName.startsWith(".")){
                    continue;
                }
                notebookNameList.add(file.getName());
            }
        }
        return ServerResponse.buildSuccessResponse(notebookNameList);
    }



    @Override
    public ServerResponse<List<NoteVo>> listNotes(String notebookName){
        return ServerResponse.buildSuccessResponse(listNotesByNotebookName(notebookName));
    }

    private List<NoteVo> listNotesByNotebookName(String notebookName) {
        File notebookDir = new File(getOrCreateUserNotebookDir(), notebookName);
        if (!notebookDir.exists() || notebookDir.isFile()){
            throw new RuntimeException("No such notebook");
        }
        File[] childFiles = notebookDir.listFiles();
        if (childFiles == null || childFiles.length == 0 ){
            return Collections.emptyList();
        }

        Set<String> modifiedSet = GitUtil.getModifiedButUnCommitted(getOrCreateUserGit(), notebookName);
        // sort by lastModifiedTime and convert
        Arrays.sort(childFiles, (f1, f2) -> (int) (
                f2.lastModified() - f1.lastModified()));
        return Arrays.stream(childFiles).
                filter(file -> !file.isDirectory())
                .filter(file -> checkExtension(file.getName()))
                .map(file -> {
                    String title = file.getName().substring(0,file.getName().lastIndexOf("."));
                    String lastModifiedDate = DateTimeUtil.dateToStr(new Date(file.lastModified()));
                    String previewContent = null;
                    Integer articleId = null;
                    NotePreviewInfo previewInfo = userNotePreviewCache.get(buildUserNoteKey(notebookName, title));
                    if (previewInfo != null) {
                        previewContent = previewInfo.getPreviewContent();
                        articleId = previewInfo.getArticleId();
                    }
                    int noteStatus = modifiedSet.contains(getRelativeFileName(notebookName, title)) ? NoteVo.STATUS_TMP_SAVED:NoteVo.STATUS_PRIVATE;
                    return new NoteVo().setNotebookName(notebookName)
                            .setTitle(title)
                            .setStatus(noteStatus)
                            .setArticleId(articleId)
                            .setLastModifiedTime(lastModifiedDate)
                            .setPreviewContent(previewContent);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ServerResponse<List<NoteVo>> search(String keyWord, List<String> searchNotebooks) {
        List<String> notebookNameList ;
        if (!CollectionUtils.isEmpty(searchNotebooks)){
            notebookNameList = searchNotebooks;
        } else {
            notebookNameList =  listNotebooks().getData();
        }
        if (CollectionUtils.isEmpty(notebookNameList)){
            return ServerResponse.buildSuccessResponse();
        }
        List<NoteVo> res = new ArrayList<>();
        notebookNameList.forEach(notebookName ->
                listNotesByNotebookName(notebookName).stream()
                .map(noteVo -> noteVo.setContent(userNoteCache.get(buildUserNoteKey(notebookName, noteVo.getTitle()))))
                .filter(noteVo -> StringUtils.isNotBlank(noteVo.getContent()) && (noteVo.getContent().contains(keyWord)
                        || noteVo.getTitle().contains(keyWord)))
                .map(noteVo -> noteVo.setSearchCount(subStrCount(noteVo.getContent(), keyWord) + subStrCount(noteVo.getTitle(), keyWord)))
                .forEach(res::add));
        res.sort((o1, o2) -> o2.getSearchCount() - o1.getSearchCount());
        return ServerResponse.buildSuccessResponse(res);

    }

    private void createNotebookIfNecessary(String notebookName){
        File notebookDir = new File(getOrCreateUserNotebookDir(),notebookName);
        if (notebookDir.exists()){
            return ;
        }
        createNotebook(notebookName);
    }

    @Override
    public ServerResponse createNotebook(String notebookName){
        File notebookDir = new File(getOrCreateUserNotebookDir(), notebookName);
        if (notebookDir.exists()) {
            throw new PromptException("笔记本已存在");
        }
        if (!notebookDir.mkdir()) {
            throw new RuntimeException("Create notebook failed");
        }

        File notebookFlagFile = new File(notebookDir, NOTEBOOK_FLAG_FILE);
        try {
            if (!notebookFlagFile.createNewFile()) {
                throw new RuntimeException("Create notebook failed");
            }
        } catch (IOException e) {
            throw new RuntimeException("Create notebook failed");
        }
        String relativeName = notebookName + "/" + NOTEBOOK_FLAG_FILE;
        GitUtil.addAndCommit(getOrCreateUserGit(), relativeName);
        log.info("create notebook: {}", notebookName);
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public ServerResponse createNote(String noteTitle, String notebookName, String content){
        File targetFile =  new File(getOrCreateUserNotebookDir(), getRelativeFileName(notebookName, noteTitle));
        if (targetFile.exists()){
            throw new RuntimeException("Note already exists");
        }
        return saveNote(noteTitle, notebookName, content);
    }

    @Override
    public ServerResponse saveNote(String noteTitle, String notebookName, String content) {

        createNotebookIfNecessary(notebookName);

        String relativeFileName = getRelativeFileName(notebookName,noteTitle);
        File noteFile = new File(getOrCreateUserNotebookDir(), relativeFileName);
        try {
            if (!noteFile.exists() && !noteFile.createNewFile()){
                throw new RuntimeException("Save note failed");
            }
        } catch (IOException e) {
            log.error("save note error", e);
            throw new RuntimeException("Save note failed");
        }

        //write file
        fileService.writeStringToFile(content,noteFile);
        GitUtil.addAndCommit(getOrCreateUserGit(),relativeFileName);

        draftNoteRepository.deleteByUsernameAndNotebookNameAndTitle(getUsername(), notebookName, noteTitle);
        invalidateCache(buildUserNoteKey(notebookName, noteTitle));
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public void tmpSaveNote(String noteTitle, String notebookName, String content) {
        String relativeFileName = getRelativeFileName(notebookName,noteTitle);
        File noteFile = new File(getOrCreateUserNotebookDir(), relativeFileName);
        if (noteFile.exists() && noteFile.isDirectory()) {
            return ;
        }
        fileService.writeStringToFile(content, noteFile);
        invalidateCache(buildUserNoteKey(notebookName, noteTitle));
    }

    @Override
    public void delTmpSavedNote(String noteTitle, String notebookName) {
        GitUtil.discardChange(getOrCreateUserGit(), getRelativeFileName(notebookName, noteTitle));
    }

    @Override
    public ServerResponse deleteNote(String notebookName, String noteTitle){
        String relativeFileName = getRelativeFileName(notebookName,noteTitle);
        File noteFile = new File(getOrCreateUserNotebookDir(), relativeFileName);
        String content = getNote(notebookName, noteTitle).getData();
        if (!noteFile.exists() || !noteFile.delete()){
            return ServerResponse.buildErrorResponse("Can't delete note");
        }
        String lastRef = GitUtil.getFileCurRef(getOrCreateUserGit(),relativeFileName);
        NotePreviewInfo previewInfo = userNotePreviewCache.get(buildUserNoteKey(notebookName, noteTitle));
        if (previewInfo.getArticleId() != null) {
            articleService.deleteArticle(previewInfo.getArticleId());
        }
        GitUtil.rmAndCommit(getOrCreateUserGit(),relativeFileName);
        delNoteRepository.save(new DelNoteDo().setNotebook(notebookName)
                                    .setTitle(noteTitle)
                                    .setLastRef(lastRef)
                                    .setContent(content)
                                    .setUsername(getUsername()));
        invalidateCache(buildUserNoteKey(notebookName, noteTitle));
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public ServerResponse recoverNote(Integer id){
        DelNoteDo delNoteDO = delNoteRepository.findByIdAndUsername(id, getUsername());
        String relativeFileName = getRelativeFileName(delNoteDO.getNotebook(), delNoteDO.getTitle());
        File noteFile = new File(getOrCreateUserNotebookDir(), relativeFileName);
        if (noteFile.exists()){
            return ServerResponse.buildErrorResponse("Note already exists");
        }
        GitUtil.recoverDeletedFile(getOrCreateUserGit(), relativeFileName, delNoteDO.getLastRef());
        return clearDelNote(id);
    }


    @Override
    public ServerResponse<String> getNote(String notebookName, String noteTitle){
        return getNote(notebookName, noteTitle, getUsername());
    }

    @Override
    public ServerResponse<String> getNote(String notebookName, String noteTitle, String username) {
        String content = userNoteCache.get(buildUserNoteKey(notebookName, noteTitle, username));
        if (content == null){
            return ServerResponse.buildErrorResponse("读取笔记失败");
        }
        return ServerResponse.buildSuccessResponse(content);
    }

    @Override
    public ServerResponse<List<NoteVersionVo>> getNoteHistory(String notebookName, String noteTitle){
        String relativeFileName = getRelativeFileName(notebookName,noteTitle);
        List<NoteVersionVo> noteVersionVoList = GitUtil.getNoteHistory(getOrCreateUserGit(),relativeFileName);
        return ServerResponse.buildSuccessResponse(noteVersionVoList);
    }

    @Override
    public ServerResponse<String> resetAndGet(String notebookName, String noteTitle, String versionRef){
        String relativeFileName = getRelativeFileName(notebookName,noteTitle);
        boolean result = GitUtil.resetAndCommit(getOrCreateUserGit(),relativeFileName,versionRef);
        if (!result){
            return ServerResponse.buildErrorResponse("Recover to history version failed");
        }
        invalidateCache(buildUserNoteKey(notebookName, noteTitle));
        return getNote(notebookName, noteTitle);
    }

    @Override
    public ServerResponse deleteNotebook(String notebookName){
        File notebookDir = new File(getOrCreateUserNotebookDir(), notebookName);
        listNotes(notebookName).getData().forEach(noteVo -> deleteNote(notebookName, noteVo.getTitle()));
        fileService.deleteFile(notebookDir);
        GitUtil.rmAndCommit(getOrCreateUserGit(),notebookName + "/" + NOTEBOOK_FLAG_FILE);
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public void renameNotebook(String srcNotebookName, String targetNotebookName) {
        File srcNotebookDir = getNotebookDir(srcNotebookName);
        if (!srcNotebookDir.exists() || srcNotebookDir.isFile()) {
            throw new PromptException("笔记本不存在");
        }

        File targetNotebookDir = getNotebookDir(targetNotebookName);
        if (targetNotebookDir.exists()) {
            throw new PromptException("目标笔记本已存在");
        }
//        if (!targetNotebookDir.mkdir()) {
//            throw new PromptException("目标笔记本无法创建");
//        }
        List<NoteVo> noteVoList = listNotes(srcNotebookName).getData();
        if (!FileUtil.renameFileOrDir(srcNotebookDir, targetNotebookDir)) {
            throw new PromptException("重命名笔记本失败");
        }
        GitUtil.rmAndCommit(getOrCreateUserGit(), srcNotebookName);
        GitUtil.addAndCommit(getOrCreateUserGit(), targetNotebookName);
        noteVoList.forEach(noteVo -> invalidateCache(buildUserNoteKey(srcNotebookName, noteVo.getTitle())));
        articleService.updateArticlesNotebookName(srcNotebookName, targetNotebookName);
    }

    /**
     * get deleted notes
     * @return
     */
    @Override
    public ServerResponse<List<DeletedNoteVo>> listDelNotes(){
        List<DeletedNoteVo> deletedNoteList = new ArrayList<>();
        delNoteRepository.findAllByUsername(getUsername())
                .forEach(delNoteDo -> deletedNoteList.add(new DeletedNoteVo()
                    .setId(delNoteDo.getId())
                    .setTitle(delNoteDo.getTitle())
                    .setNotebook(delNoteDo.getNotebook())
                    .setLastRef(delNoteDo.getLastRef())
                    .setUsername(delNoteDo.getUsername())
                    .setContent(delNoteDo.getContent())
        ));
        return ServerResponse.buildSuccessResponse(deletedNoteList);
    }

    @Override
    public ServerResponse clearDelNote(@NonNull Integer id){
        delNoteRepository.deleteByIdAndUsername(id, getUsername());
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public ServerResponse copyNote(String srcNotebook, String targetNotebook, String title) {
        if (srcNotebook.equals(targetNotebook)){
            throw new RuntimeException("Same notebook");
        }
        ServerResponse<String> response = getNote(srcNotebook, title);
        if (!response.isSuccess()){
            return response;
        }
        String content = response.getData();
        return createNote(title, targetNotebook, content);
    }

    @Override
    public ServerResponse moveNote(String srcNotebook, String srcTitle, String targetNotebook, String targetTitle){
        // src != target
        if (srcNotebook.equalsIgnoreCase(targetNotebook) && srcTitle.equalsIgnoreCase(targetTitle)){
            throw new IllegalArgumentException();
        }
        if (StringUtils.isAnyBlank(srcNotebook, srcTitle, targetNotebook, targetTitle)){
            throw new IllegalArgumentException();
        }
        ServerResponse<String> response = getNote(srcNotebook, srcTitle);
        if (!response.isSuccess()){
            return response;
        }
        String content = response.getData();
        String targetRelativeName = getRelativeFileName(targetNotebook, targetTitle);
        File targetFile =  new File(getOrCreateUserNotebookDir(), targetRelativeName);
        if (targetFile.exists()){
            throw new RuntimeException("Note already exists");
        }
        String srcRelativeName = getRelativeFileName(srcNotebook, srcTitle);
        File srcFile = new File(getOrCreateUserNotebookDir(), srcRelativeName);
        fileService.deleteFile(srcFile);
        fileService.writeStringToFile(content, targetFile);
        NotePreviewInfo previewInfo = userNotePreviewCache.get(buildUserNoteKey(srcNotebook, srcTitle, getUsername()));
        // 先移动记录
        if (previewInfo.getArticleId() != null) {
            articleService.moveArticle(previewInfo.getArticleId(), targetNotebook, targetTitle);
        }
        GitUtil.mvAndCommit(getOrCreateUserGit(), srcRelativeName, targetRelativeName);
        invalidateCache(buildUserNoteKey(srcNotebook, srcTitle));
        userNoteCache.put(buildUserNoteKey(targetNotebook, targetTitle), content);
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    @Transactional
    public ServerResponse clearAllDelNotes(){
        delNoteRepository.deleteAllByUsername(getUsername());
        return ServerResponse.buildSuccessResponse();
    }

    private String getRelativeFileName(String notebookName, String noteTitle) {

        return notebookName + "/" + noteTitle+".md";
    }

    private File getOrCreateUserNotebookDir(){
        File dir = new File(notesDir, getUsername());
        if (dir.exists()){
            return dir;
        }
        dir.mkdir();
        return dir;
    }

    private String getUsername(){
        return ThreadLocalUtil.getUsername();
    }

    private File getUserNotebookDir(){
        File dir = new File(notesDir, ThreadLocalUtil.getUsername());
        if (dir.exists()){
            return dir;
        }
        return null;
    }

    private Git getOrCreateUserGit(){
        return GitUtil.getOrInitGit(getOrCreateUserNotebookDir());
    }

    private boolean checkExtension(String filename){
        return filename.endsWith(".md")
                || filename.endsWith(".MD")
                || filename.endsWith(".mD")
                || filename.endsWith(".Md");
    }

    private UserNoteKey buildUserNoteKey(String notebookName, String noteTitle){
        return buildUserNoteKey(notebookName, noteTitle, getUsername());
    }

    private UserNoteKey buildUserNoteKey(String notebookName, String noteTitle, String username){
        return new UserNoteKey().setNotebookName(notebookName).setNoteTitle(noteTitle).setUsername(username);
    }


    private int subStrCount(String target, String substr){
      int count = 0;
      int startIndex = 0;
      int searchIndex ;
      while (( searchIndex = target.indexOf(substr, startIndex)) != -1){
          count ++;
          startIndex = searchIndex + substr.length();
        }
      return count;
    }

    void invalidateCache(UserNoteKey key){
        userNotePreviewCache.invalidate(key);
        userNoteCache.invalidate(key);
    }

    File getNotebookDir(String notebookName) {
        return new File(getOrCreateUserNotebookDir(), notebookName);
    }
}
