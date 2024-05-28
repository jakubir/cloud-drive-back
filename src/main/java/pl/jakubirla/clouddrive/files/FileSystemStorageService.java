package pl.jakubirla.clouddrive.files;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import pl.jakubirla.clouddrive.config.StorageProperties;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileSystemStorageService {
    private Path rootLocation;

    public FileSystemStorageService(StorageProperties properties) {
        if (properties.location().trim().isEmpty())
            throw new RuntimeException("File location must be provided");

        this.rootLocation = Path.of(properties.location());
    }

    public void store(MultipartFile files[], String storageDirectory) {
        Path fullStoragePath = Path.of(this.rootLocation + "\\" + storageDirectory);

        for (MultipartFile file : files) {
            String[] pathParts = file.getOriginalFilename().split("/");

            if (pathParts.length > 1) {
                String directoryPath = storageDirectory + "\\" + String.join("\\", Arrays.copyOf(pathParts, pathParts.length - 1));
                
                if (!isDirectoryNameTaken(directoryPath)) 
                    createDirectory(directoryPath);
            }

            try {
                if (file.isEmpty())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");

                Path destinationFile = Path.of(fullStoragePath + "\\" + file.getOriginalFilename());

                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file: ");
            }
        }
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

    public boolean isDirectory(String path) {

        return Path.of(this.rootLocation + "\\" + path).toFile().isDirectory();
    }

    public Stream<FileToRead> loadAll(String userFolder) {
        return loadAll(Path.of(this.rootLocation + "\\" + userFolder));
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read stored files");
        }
    }

    public void createDirectory(String directoryPath) {
        try {
            Path path = Path.of(this.rootLocation + "\\" + directoryPath);

            if (!Files.exists(path))
                Files.createDirectories(path);
            else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Directory already exists");

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create directory");
        }
    }

    private boolean isDirectoryNameTaken(String directoryPath) {
        Path path = Path.of(this.rootLocation + "\\" + directoryPath);

        return Files.exists(path);
    }

    public void removeFile(String filePath) {
        try {
            Path path = Path.of(this.rootLocation + "\\" + filePath);

            if (!Files.exists(path))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File already removed");

            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove file");
        }
    }

    public void moveElement(String elementToMove, String targetElement) {
        Path elementToMovePath = Path.of(this.rootLocation + "\\" + elementToMove);
        Path targetPath = Path.of(this.rootLocation + "\\" + targetElement);

        try {
            Files.move(elementToMovePath, targetPath);
        } catch (IOException e) {
            throw new RuntimeException("can't_move");
        }
    }

    public void downloadElement(String elementPath, OutputStream output) throws IOException {
        Path path = Path.of(this.rootLocation + "\\" + elementPath);

        if (path.toFile().isFile()) {
            Files.copy(path, output);
            return;
        }

        ZipOutputStream zipOutput = new ZipOutputStream(output);

        zipResource(path.toFile(), path.toFile().getName(), zipOutput);
        zipOutput.close();
    }

    private void zipResource(File file, String fileName, ZipOutputStream zipOutput) throws IOException {
        if (file.isDirectory()) {
            String name = fileName + (fileName.endsWith("/") ? "" : "/");

            zipOutput.putNextEntry(new ZipEntry(name));
            zipOutput.closeEntry();

            for (File child : file.listFiles())
                zipResource(child, fileName + "/" + child.getName(), zipOutput);

            return;
        }

        FileInputStream fileInput = new FileInputStream(file);
        byte[] bytes = new byte[1024];
        int length;

        zipOutput.putNextEntry(new ZipEntry(fileName));

        while ((length = fileInput.read(bytes)) >= 0)
            zipOutput.write(bytes, 0, length);

        fileInput.close();
    }
}