package ink.markidea.note.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author hansanshi
 * @date 2020/1/29
 */
@Slf4j
public class FileUtil {

    public static long KB_LIMIT = 1000;

    public static long MB_LIMIT = 1000 * KB_LIMIT;

    public static long GB_LIMIT = 1000 * MB_LIMIT;

    public static long TB_LIMIT = 1000 * GB_LIMIT;

    public static List<String> readLines(File file, int size) {
        try (FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            List<String> result = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null && size > 0) {
                if (StringUtils.isBlank(line)){
                    continue;
                }
                result.add(line);
                size --;
            }
            return result;
        } catch (IOException e) {
            log.error("readLine", e);
            return null;
        }
    }

    public static String readFileAsString(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return new String(bytes);
        } catch (IOException e) {
            log.error("read file:{} failed, cause is", file.getAbsolutePath(), e);
            return null;
        }
    }


    public static boolean writeStringToFile(String content, File targetFile) {
        try (Writer writer = new FileWriter(targetFile);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.write(content);
            bufferedWriter.flush();
            return true;
        } catch (IOException e) {
            log.error("write to file:{} failed, cause is", targetFile.getAbsolutePath(), e);
            return false;
        }
    }


    public static void deleteFileOrDirectory(File file) {
        if (!file.exists()) {
            return ;
        }
        if (file.isFile()) {
            file.delete();
            return ;
        }
        File[] childFiles = file.listFiles();
        if (childFiles != null) {
            for (File childFile : childFiles) {
                deleteFileOrDirectory(childFile);
            }
        }
        file.delete();
    }

    public static void copyDirectory(File srcDir, File targetDir) {
        if (!srcDir.exists()) {
            throw new IllegalStateException("srcDir doesn't exist");
        }
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        if (srcDir.isFile() || targetDir.isFile()) {
            throw new IllegalArgumentException("Only directory");
        }

        File[] childFileList = srcDir.listFiles();
        if (childFileList == null || childFileList.length == 0) {
            return;
        }
        for (File childFile : childFileList) {
            if (childFile.isDirectory()) {
                copyDirectory(childFile, new File(targetDir, childFile.getName()));
                continue;
            }
            copyFile(childFile, new File(targetDir, childFile.getName()));
        }
    }

    private static void copyFile(File srcFile, File destFile) {
        try {
            Files.copy(srcFile.toPath(), destFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("copy file failed");
        }
    }


    public static void unzip(InputStream fileInputStream, String destPath) {
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);

        try {
            ZipEntry entry = null;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(destPath, entry.getName()).mkdir();
                    zipInputStream.closeEntry();
                    continue;
                }
                FileOutputStream out = new FileOutputStream(new File(destPath, entry.getName()));
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = zipInputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.close();
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            // do nothing
        }

    }

    /**
     * 根据文件大小获取字符串
     * @param file
     * @return
     */
    public static String getFileSizeStr(File file){

        long fileSize = file.length();
        if (fileSize < KB_LIMIT){
            return fileSize + "B";
        }

        if (fileSize < MB_LIMIT){
            return new DecimalFormat("#.00").format(fileSize/1000.0) + "KB";
        }

        if (fileSize < GB_LIMIT){
            return new DecimalFormat("#.00").format(fileSize/1000.0/1000.0) + "MB";
        }

        if (fileSize < TB_LIMIT){
            return new DecimalFormat("#.00").format(fileSize/1000.0/1000.0) + "GB";

        }
        return new DecimalFormat("#.00").format(fileSize/1000.0/1000.0) + "TB";

    }

}
