package pl.jakubirla.clouddrive.files;

import com.nimbusds.jose.util.Base64URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FilesController {

    private final FileSystemStorageService storageService;

    @Autowired
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
            System.out.println(e.getMessage());

            try {
                storageService.createDirectory("/" + userFolder);
            } catch (Exception e2) {
                System.out.println(e2.getMessage());
            }
        }

        return "{" +
                "\"name\": \"root\"" +
                ",\"type\": \"directory\"" +
                ",\"children\": [" + (rootChildren == null ? "" : rootChildren) +
                "]}";
    }

    @PostMapping("")
    public String fileUpload(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userFolder = Base64URL.encode(authentication.getName()).toString();

        storageService.store(file, userFolder);

        return file.getName() + " file stored";
    }
}