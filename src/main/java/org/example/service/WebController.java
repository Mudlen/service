package org.example.service;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

@Controller
public class WebController {

    private ServiceStorage serviceStorage = new ServiceStorage(5);
    private final ArrayList<String> consoleLog = new ArrayList<>();

    @GetMapping
    public String main(Map<String, Object> model){
        model.put("consoleLog", consoleLog);
        return "main";
    }

    @PostMapping("get")
    public String get(@RequestParam String keyGet, @RequestParam String status, Map<String,Object> model){
        model.put("consoleLog",consoleLog);
        if(status.equals("Операция получения (get) прервана: не правильно введен ключ")){
            consoleLog.add(status);
        }else {
            String data = serviceStorage.getData(keyGet);
            if(data != null){
                consoleLog.add(status + data);
            }else {
                consoleLog.add("По полученному ключу ("+keyGet+") данных не найдено.");
            }
        }
        model.put("consoleLog",consoleLog);
        return "main";
    }

    @PostMapping("set")
    public String set(@RequestParam String keySet,
                      @RequestParam String dataSet,
                      @RequestParam long ttlSet,
                      @RequestParam String status,
                      Map<String, Object> model){
        consoleLog.add(status);
        model.put("consoleLog",consoleLog);
        if(status.equals("Операция записи (set) прошла успешно")){
            serviceStorage.addData(keySet, dataSet, ttlSet);
        }
        return "main";
    }

    @PostMapping("del")
    public String del(@RequestParam String keyDel, @RequestParam String status, Map<String, Object> model){
        model.put("consoleLog",consoleLog);
        if(status.equals("Операция удаления (remove) прервана: не правильно введен ключ")){
            consoleLog.add(status);
        }else {
            String deletedData = serviceStorage.remove(keyDel);
            if(deletedData != null){
                consoleLog.add(status + deletedData);
            }else {
                consoleLog.add("По полученному ключу ("+keyDel+") данных не найдено.");
            }
        }
        model.put("consoleLog",consoleLog);
        return "main";
    }

    @PostMapping("dump")
    public ResponseEntity<Resource> dump(Map<String, Object> model) throws IOException {
        model.put("consoleLog",consoleLog);
        serviceStorage.createDumpFile();
        File file = new File("C:\\Users\\Public\\service\\ServiceStorage.txt");
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ServiceStorage.txt");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("load")
    public String load(@RequestParam("file") MultipartFile file, Map<String,Object> model) throws IOException {
        model.put("consoleLog",consoleLog);
        if(!file.isEmpty()){
            byte[] fileBytes = file.getBytes();
            String jsonFromFile = new String(fileBytes);
            try {
                serviceStorage = ServiceStorage.fromJsonToServiceStorage(jsonFromFile);
            }catch (NullPointerException nullPointerException){
                consoleLog.add("Ошибка загрузки хранилища");
                return "main";
            }
            consoleLog.add("Загрузка хранилища прошла успешно");
        }else {
            consoleLog.add("Ошибка загрузки хранилища");
        }
        model.put("consoleLog",consoleLog);
        return "main";
    }

}
