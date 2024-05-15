package pl.jakubirla.clouddrive.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.jakubirla.clouddrive.config.StorageProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService {
    private Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        if (properties.location().trim().isEmpty())
            throw new RuntimeException("File location must be provided");

        this.rootLocation = Path.of(properties.location());
    }

    public void store(MultipartFile file, String userFolder) {
        Path userRootLocation = Path.of(this.rootLocation + "/" + userFolder);

        try {
            if (file.isEmpty()) throw new RuntimeException("Failed to store empty file.");

            Path destinationFile = userRootLocation.resolve(Paths.get(file.getOriginalFilename())).normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(userRootLocation.toAbsolutePath())) {
                // This is a security check
                throw new RuntimeException("Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to store file.");
        }
    }

    public Stream<FileToRead> loadAll(String userFolder) {
        return loadAll(Path.of(this.rootLocation + "/" + userFolder));
    }

    private long directorySize(Path path) {
        try {
            return Files.walk(path)
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        } catch (IOException e) {
            return 0;
        }
    }

    private Stream<FileToRead> loadAll(Path location) {
        try {
            return Files.walk(location, 1)
                    .filter(path -> !path.equals(location))
                    .map(path ->
                        new FileToRead(
                            path.getFileName().toString(),
                            path.toFile().isDirectory() ? "directory" : "file",
                            path.toFile().lastModified(),
                            path.toFile().isDirectory() ? directorySize(path) : path.toFile().length(),
                            path.toFile().isDirectory() ? this.loadAll(path).toList() : null
                        )
                    );
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read stored files", e);
        }
    }

    public void createDirectory(String directoryPath) {
        try {
            Path path = Path.of(this.rootLocation + "/" + directoryPath);

            if (!Files.exists(path))
                Files.createDirectories(path);

        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + directoryPath);
        }
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not initialize storage");
        }
    }
}