package ink.markidea.note.context.config;

import com.fasterxml.jackson.core.type.TypeReference;
import ink.markidea.note.entity.dto.WebsiteConfigDto;
import ink.markidea.note.util.FileUtil;
import ink.markidea.note.util.JsonUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * load path
 * @author hansanshi
 * @date 2019/12/21
 */
public class ContextPropertyLoader implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        ApplicationHome home =new ApplicationHome(getClass());

        Map<String,Object> map = new HashMap<>();

        String path ;
        path = home.getDir().getAbsolutePath();
        addPathAndInitDir(map, path);
        PropertySource propertySource = new MapPropertySource("markidea-sys",map);
        environment.getPropertySources().addLast(propertySource);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 9;
    }

    private void addPathAndInitDir(Map<String,Object> map, String basePath){
        map.put("baseDir",basePath);

        // dir store sqlite database
        File dbDir = new File(basePath, "db");
        checkAndCreateDirIfNecessary(dbDir);
        map.put("dbDir", dbDir.getAbsolutePath());


        // dir store private keys
        File prvKeysDir = new File(basePath, ".ssh");
        checkAndCreateDirIfNecessary(prvKeysDir);
        map.put("sshKeysDir", prvKeysDir.getAbsolutePath());

        File notesDir = new File(basePath, "notes");
        checkAndCreateDirIfNecessary(notesDir);
        map.put("notesDir", notesDir.getAbsolutePath());

        // the directory store file
        File staticDir = new File(basePath,"static");
        checkAndCreateDirIfNecessary(staticDir);
        map.put("staticDir", staticDir.getAbsolutePath());

        // front resource
        File frontDir = new File(basePath, "front");
        checkAndCreateDirIfNecessary(frontDir);
        transferFrontResourceIfNecessary(new ClassPathResource("front.zip"), frontDir);
        map.put("frontDir", frontDir.getAbsolutePath());


        File fileDir = new File(staticDir, "file");
        checkAndCreateDirIfNecessary(fileDir);
        map.put("fileDir", fileDir.getAbsolutePath());

        // config dir

        File configDir = new File(staticDir, "config");
        checkAndCreateDirIfNecessary(configDir);
        map.put("configDir", configDir.getAbsolutePath());

        // load website config file
        File websiteConfigFile = new File(configDir, "website-config.json");
        WebsiteConfigDto websiteConfig ;
        if (websiteConfigFile.exists()) {
            String jsonStr = FileUtil.readFileAsString(websiteConfigFile);
            websiteConfig = JsonUtil.stringToObj(jsonStr, WebsiteConfigDto.class);
            File indexHtmlFile = new File(frontDir, "index.html");
            String indexHtmlStr  = FileUtil.readFileAsString(indexHtmlFile);
            String newIndexHtml = indexHtmlStr.replace("<title>" + "MarkIdea" + "</title>", "<title>" + websiteConfig.getWebsiteTitle() + "</title>");
            FileUtil.writeStringToFile(newIndexHtml, indexHtmlFile);
        } else {
            websiteConfig = new WebsiteConfigDto();
        }

        Map<String, String> configProperties = JsonUtil.stringToObj(JsonUtil.objToString(websiteConfig), new TypeReference<Map<String, String>>() {});
        assert configProperties != null;
        map.putAll(configProperties);


    }

    private void transferFrontResourceIfNecessary(ClassPathResource resource, File frontDir) {
        if (frontDir.listFiles() != null && frontDir.listFiles().length > 0){
            FileUtil.deleteChildFiles(frontDir);
        }
        if (!resource.exists()){
            return ;
        }
        try {
            FileUtil.unzip(resource.getInputStream(), frontDir.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Can't init front dir");
        }
    }

    private void checkAndCreateDirIfNecessary(File dir) {
        if (dir.exists()){
            if (dir.isDirectory()){
                return ;
            }
            throw new IllegalStateException("not a directory");
        }

        if (!dir.mkdir()){
            throw new IllegalStateException("create a directory failed");

        }
    }
}
