package br.com.fernando.file.api.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import br.com.fernando.file.api.execption.FileStorageException;
import br.com.fernando.file.api.property.FileStorageProperties;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
            .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Não foi possível criar o diretório em que os arquivos enviados serão armazenados.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // padroniza o nome do arquivo
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Verifica se o nome do arquivo contém caracteres inválidos
            if (fileName.contains("..")) {
                throw new FileStorageException("Desculpe! Nome do arquivo contém sequência de caminho inválida" + fileName);
            }

            //  Copiar arquivo para o local de destino (Substituindo arquivo existente pelo mesmo nome)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Não foi possivel armazenar o arquivo " + fileName + ". Por favor tente novamente!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("Arquivo não encontrado " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Arquivo não encontrado " + fileName, ex);
        }
    }
}
