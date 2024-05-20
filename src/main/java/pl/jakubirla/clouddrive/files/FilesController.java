package pl.jakubirla.clouddrive.files;

import com.nimbusds.jose.util.Base64URL;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FilesController {

    private final FileSystemStorageService storageService;

    public FilesController(FileSystemStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/list")
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

    @PostMapping("")
    public void fileUpload(@RequestParam("files") MultipartFile files[], @RequestParam("path") String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userFolder = Base64URL.encode(authentication.getName()).toString();
        String storageDirectory = userFolder + "/" + path;

        storageService.store(files, storageDirectory);
    }
}