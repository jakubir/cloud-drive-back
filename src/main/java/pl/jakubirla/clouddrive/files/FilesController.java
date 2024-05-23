package pl.jakubirla.clouddrive.files;

import com.nimbusds.jose.util.Base64URL;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.stream.Collectors;


@RestController
public class FilesController {

    private final FileSystemStorageService storageService;

    public FilesController(FileSystemStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/files")
    public String fileList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String rootChildren = null;
        String userFolder = Base64URL.encode(authentication.getName()).toString();

        try {
            rootChildren = storageService.loadAll(userFolder)
                    .map(FileToRead::toString)
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            storageService.createDirectory("/" + userFolder);
        }

        return "{" +
                "\"name\": \"root\"" +
                ",\"type\": \"directory\"" +
                ",\"children\": [" + (rootChildren == null ? "" : rootChildren) +
                "]}";
    }

    @GetMapping("/files/{path}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable("path") String encodedPath) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userFolder = Base64URL.encode(authentication.getName()).toString();
        String path = Base64URL.from(encodedPath).decodeToString();
        String storageDirectory = userFolder + "/" + path;

//        Resource resource = storageService.downloadElement(storageDirectory);
        String fileName = path.split("/")[path.split("/").length - 1];
        fileName = storageService.isDirectory(storageDirectory) ? fileName + ".zip" : fileName;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(output -> {
                    storageService.downloadElement(storageDirectory, output);
                });
    }

    @PostMapping("/files")
    public void fileUpload(@RequestParam("files") MultipartFile files[], @RequestParam("path") String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userFolder = Base64URL.encode(authentication.getName()).toString();
        String storageDirectory = userFolder + "/" + path;

        storageService.store(files, storageDirectory);
    }

    @PostMapping("/files/new-folder")
    public void createNewDirectory(@RequestParam("path") String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userFolder = Base64URL.encode(authentication.getName()).toString();
        String storageDirectory = userFolder + "/" + path;
        
        storageService.createDirectory("/" + storageDirectory);
    }
    
    @DeleteMapping("/files")
    public void removeFile(@RequestParam("path") String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userFolder = Base64URL.encode(authentication.getName()).toString();
        String storageDirectory = userFolder + "/" + path;

        storageService.removeFile("/" + storageDirectory);
    }

    @PatchMapping("/files")
    public void renameElement(@RequestParam("path") String path, @RequestParam("newName") String newName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userFolder = Base64URL.encode(authentication.getName()).toString();

        String[] pathParts = path.split("/");
        pathParts[pathParts.length - 1] = newName;
        String target = userFolder + "/" + String.join("/", pathParts);
        String elementToChange = userFolder + "/" + path;

        try {
            storageService.moveElement(elementToChange, target);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to move the element");
        }
    }
}