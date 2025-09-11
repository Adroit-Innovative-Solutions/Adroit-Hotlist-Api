package com.adroit.hotlistmicroservice.filevalidator;

import com.adroit.hotlistmicroservice.exception.InvalidFileException;
import com.adroit.hotlistmicroservice.exception.InvalidFileTypeException;
import com.adroit.hotlistmicroservice.exception.MaxUploadSizeExceededException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class FileValidator {

        private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "doc");
        private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
                "application/msword" // DOC
        );

        // Max file size: 20MB
        private static final long MAX_FILE_SIZE = 60 * 1024 * 1024;

        public static void validateStrictFile(MultipartFile file) {
            if (file.isEmpty()) {
                throw new InvalidFileException("File cannot be empty");
            }
            validateFileSize(file);
            validateStrictFileType(file);
        }

        private static void validateFileSize(MultipartFile file) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new MaxUploadSizeExceededException(
                        "File size exceeds 20MB limit. Actual size: " +
                                (file.getSize() / (1024 * 1024)) + "MB"
                );
            }
        }

        private static void validateStrictFileType(MultipartFile file) {
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();

            // 1. Check file extension
            if (fileName == null || !isValidExtension(fileName)) {
                throw new InvalidFileTypeException(
                        "Only PDF (.pdf), Word (.docx, .doc) files are allowed"
                );
            }

            // 2. Verify MIME type matches allowed types
            if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
                throw new InvalidFileTypeException(
                        "Invalid file content type. Detected: " + contentType
                );
            }
        }

        private static boolean isValidExtension(String fileName) {
            String extension = getFileExtension(fileName).toLowerCase();
            return ALLOWED_EXTENSIONS.contains(extension);
        }

        private static String getFileExtension(String fileName) {
            int lastDot = fileName.lastIndexOf(".");
            return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
        }

    public static String mapFileNameToFileType(String fileName) {

            if(fileName.endsWith(".pdf"))
                return "PDF";
            else if (fileName.endsWith(".doc"))
                return "DOC";
            else if (fileName.endsWith(".docx"))
                return "DOCX";
            else return "UNKNOWN";
    }
}
