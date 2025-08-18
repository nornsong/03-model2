# 05. Java IO 스트림 실무 활용 (Practical Usage)

## 🎯 학습 목표

- 파일 복사, 이동, 삭제 등의 기본 파일 조작 방법 학습
- 대용량 파일 처리 전략과 최적화 기법 파악
- 에러 처리와 리소스 관리의 중요성 이해
- 성능 최적화를 위한 실무 팁 습득

## 📁 파일 조작 기본 기능

### 1. 파일 복사 (Copy)

#### 기본 파일 복사

```java
import java.io.*;
import java.nio.file.*;

public class FileCopyExample {
    public static void main(String[] args) {
        String sourceFile = "source.txt";
        String targetFile = "target.txt";

        try {
            // 방법 1: Files.copy 사용 (Java 7+)
            Files.copy(Paths.get(sourceFile), Paths.get(targetFile),
                      StandardCopyOption.REPLACE_EXISTING);

            System.out.println("파일 복사 완료 (Files.copy)");

        } catch (IOException e) {
            System.err.println("파일 복사 실패: " + e.getMessage());
        }
    }
}
```

#### 스트림을 사용한 파일 복사

```java
import java.io.*;

public class StreamFileCopyExample {
    public static void main(String[] args) {
        String sourceFile = "source.txt";
        String targetFile = "target.txt";

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetFile)) {

            byte[] buffer = new byte[8192]; // 8KB 버퍼
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            System.out.println("파일 복사 완료! 총 " + totalBytes + " 바이트");

        } catch (IOException e) {
            System.err.println("파일 복사 실패: " + e.getMessage());
        }
    }
}
```

#### 진행률 표시가 있는 파일 복사

```java
import java.io.*;

public class ProgressFileCopyExample {
    public static void main(String[] args) {
        String sourceFile = "large_file.txt";
        String targetFile = "copy_large_file.txt";

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetFile)) {

            File source = new File(sourceFile);
            long totalSize = source.length();
            long copiedSize = 0;

            byte[] buffer = new byte[8192];
            int bytesRead;

            System.out.println("파일 복사 시작...");
            System.out.println("전체 크기: " + totalSize + " 바이트");

            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                copiedSize += bytesRead;

                // 진행률 계산 및 표시
                double progress = (double) copiedSize / totalSize * 100;
                System.out.printf("\r진행률: %.1f%% (%d/%d 바이트)",
                    progress, copiedSize, totalSize);
            }

            System.out.println("\n파일 복사 완료!");

        } catch (IOException e) {
            System.err.println("파일 복사 실패: " + e.getMessage());
        }
    }
}
```

### 2. 파일 이동 (Move)

#### 파일 이동 및 이름 변경

```java
import java.io.*;
import java.nio.file.*;

public class FileMoveExample {
    public static void main(String[] args) {
        String sourceFile = "old_name.txt";
        String targetFile = "new_name.txt";

        try {
            // 방법 1: Files.move 사용 (Java 7+)
            Files.move(Paths.get(sourceFile), Paths.get(targetFile),
                      StandardCopyOption.REPLACE_EXISTING);

            System.out.println("파일 이동 완료 (Files.move)");

        } catch (IOException e) {
            System.err.println("파일 이동 실패: " + e.getMessage());
        }
    }
}
```

#### 스트림을 사용한 파일 이동 (복사 후 삭제)

```java
import java.io.*;

public class StreamFileMoveExample {
    public static void main(String[] args) {
        String sourceFile = "source.txt";
        String targetFile = "target.txt";

        try {
            // 1. 파일 복사
            copyFile(sourceFile, targetFile);

            // 2. 원본 파일 삭제
            File source = new File(sourceFile);
            if (source.delete()) {
                System.out.println("파일 이동 완료!");
            } else {
                System.out.println("원본 파일 삭제 실패");
            }

        } catch (IOException e) {
            System.err.println("파일 이동 실패: " + e.getMessage());
        }
    }

    private static void copyFile(String source, String target) throws IOException {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(target)) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
}
```

### 3. 파일 삭제 (Delete)

#### 파일 삭제

```java
import java.io.*;

public class FileDeleteExample {
    public static void main(String[] args) {
        String fileName = "temp_file.txt";

        try {
            File file = new File(fileName);

            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("파일 삭제 완료: " + fileName);
                } else {
                    System.out.println("파일 삭제 실패: " + fileName);
                }
            } else {
                System.out.println("파일이 존재하지 않음: " + fileName);
            }

        } catch (Exception e) {
            System.err.println("파일 삭제 중 오류: " + e.getMessage());
        }
    }
}
```

#### 디렉토리 및 하위 파일 삭제

```java
import java.io.*;
import java.nio.file.*;

public class DirectoryDeleteExample {
    public static void main(String[] args) {
        String directoryPath = "temp_directory";

        try {
            Path directory = Paths.get(directoryPath);

            if (Files.exists(directory)) {
                // 방법 1: Files.walk 사용 (Java 7+)
                Files.walk(directory)
                     .sorted((a, b) -> b.compareTo(a)) // 하위 파일부터 삭제
                     .forEach(path -> {
                         try {
                             Files.deleteIfExists(path);
                             System.out.println("삭제됨: " + path);
                         } catch (IOException e) {
                             System.err.println("삭제 실패: " + path + " - " + e.getMessage());
                         }
                     });

                System.out.println("디렉토리 삭제 완료: " + directoryPath);
            } else {
                System.out.println("디렉토리가 존재하지 않음: " + directoryPath);
            }

        } catch (IOException e) {
            System.err.println("디렉토리 삭제 중 오류: " + e.getMessage());
        }
    }
}
```

## 🚀 대용량 파일 처리 전략

### 1. 청크 단위 처리

#### 청크 단위 파일 복사

```java
import java.io.*;

public class ChunkFileCopyExample {
    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB

    public static void main(String[] args) {
        String sourceFile = "very_large_file.dat";
        String targetFile = "copy_large_file.dat";

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetFile)) {

            File source = new File(sourceFile);
            long totalSize = source.length();
            long copiedSize = 0;

            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            System.out.println("대용량 파일 복사 시작...");
            System.out.println("전체 크기: " + totalSize + " 바이트");
            System.out.println("청크 크기: " + CHUNK_SIZE + " 바이트");

            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                copiedSize += bytesRead;

                // 진행률 표시
                double progress = (double) copiedSize / totalSize * 100;
                System.out.printf("\r진행률: %.1f%% (%d/%d 바이트)",
                    progress, copiedSize, totalSize);

                // 메모리 정리 (가비지 컬렉션 유도)
                if (copiedSize % (CHUNK_SIZE * 10) == 0) {
                    System.gc();
                }
            }

            System.out.println("\n대용량 파일 복사 완료!");

        } catch (IOException e) {
            System.err.println("파일 복사 실패: " + e.getMessage());
        }
    }
}
```

### 2. 병렬 처리

#### 병렬 파일 복사

```java
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ParallelFileCopyExample {
    private static final int THREAD_COUNT = 4;
    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB

    public static void main(String[] args) {
        String sourceFile = "very_large_file.dat";
        String targetFile = "parallel_copy_file.dat";

        try {
            File source = new File(sourceFile);
            long totalSize = source.length();
            long chunkCount = (totalSize + CHUNK_SIZE - 1) / CHUNK_SIZE;

            System.out.println("병렬 파일 복사 시작...");
            System.out.println("전체 크기: " + totalSize + " 바이트");
            System.out.println("청크 수: " + chunkCount);
            System.out.println("스레드 수: " + THREAD_COUNT);

            // 스레드 풀 생성
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            AtomicLong copiedSize = new AtomicLong(0);

            // 각 청크를 별도 스레드에서 처리
            for (int i = 0; i < chunkCount; i++) {
                final int chunkIndex = i;
                executor.submit(() -> {
                    try {
                        copyChunk(sourceFile, targetFile, chunkIndex, CHUNK_SIZE, totalSize);
                        long current = copiedSize.addAndGet(CHUNK_SIZE);
                        double progress = (double) current / totalSize * 100;
                        System.out.printf("\r진행률: %.1f%% (%d/%d 바이트)",
                            progress, current, totalSize);
                    } catch (IOException e) {
                        System.err.println("청크 " + chunkIndex + " 복사 실패: " + e.getMessage());
                    }
                });
            }

            // 모든 작업 완료 대기
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);

            System.out.println("\n병렬 파일 복사 완료!");

        } catch (Exception e) {
            System.err.println("병렬 파일 복사 실패: " + e.getMessage());
        }
    }

    private static void copyChunk(String sourceFile, String targetFile,
                                 int chunkIndex, int chunkSize, long totalSize) throws IOException {
        long startPos = (long) chunkIndex * chunkSize;
        long endPos = Math.min(startPos + chunkSize, totalSize);
        int actualChunkSize = (int) (endPos - startPos);

        try (RandomAccessFile source = new RandomAccessFile(sourceFile, "r");
             RandomAccessFile target = new RandomAccessFile(targetFile, "rw")) {

            source.seek(startPos);
            target.seek(startPos);

            byte[] buffer = new byte[actualChunkSize];
            source.read(buffer);
            target.write(buffer);
        }
    }
}
```

### 3. 메모리 매핑 (Memory Mapping)

#### NIO를 사용한 메모리 매핑

```java
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

public class MemoryMappedFileExample {
    public static void main(String[] args) {
        String sourceFile = "large_file.dat";
        String targetFile = "mapped_copy.dat";

        try {
            Path sourcePath = Paths.get(sourceFile);
            Path targetPath = Paths.get(targetFile);

            // 파일 크기 확인
            long fileSize = Files.size(sourcePath);
            System.out.println("파일 크기: " + fileSize + " 바이트");

            // 메모리 매핑을 사용한 파일 복사
            try (FileChannel sourceChannel = FileChannel.open(sourcePath, StandardOpenOption.READ);
                 FileChannel targetChannel = FileChannel.open(targetPath,
                     StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

                long position = 0;
                long remaining = fileSize;

                while (remaining > 0) {
                    // 청크 크기 계산 (최대 100MB)
                    long chunkSize = Math.min(remaining, 100 * 1024 * 1024);

                    // 메모리 매핑
                    MappedByteBuffer sourceBuffer = sourceChannel.map(
                        FileChannel.MapMode.READ_ONLY, position, chunkSize);
                    MappedByteBuffer targetBuffer = targetChannel.map(
                        FileChannel.MapMode.READ_WRITE, position, chunkSize);

                    // 데이터 복사
                    targetBuffer.put(sourceBuffer);

                    position += chunkSize;
                    remaining -= chunkSize;

                    // 진행률 표시
                    double progress = (double) (fileSize - remaining) / fileSize * 100;
                    System.out.printf("\r진행률: %.1f%% (%d/%d 바이트)",
                        progress, fileSize - remaining, fileSize);
                }

                System.out.println("\n메모리 매핑 파일 복사 완료!");

            }

        } catch (IOException e) {
            System.err.println("메모리 매핑 파일 복사 실패: " + e.getMessage());
        }
    }
}
```

## ⚠️ 에러 처리와 리소스 관리

### 1. 체계적인 예외 처리

#### 예외 처리 예제

```java
import java.io.*;

public class RobustFileOperationExample {
    public static void main(String[] args) {
        String sourceFile = "source.txt";
        String targetFile = "target.txt";

        try {
            copyFileWithErrorHandling(sourceFile, targetFile);
        } catch (FileNotFoundException e) {
            System.err.println("파일을 찾을 수 없습니다: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("보안 권한이 없습니다: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("파일 I/O 오류: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("예상치 못한 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void copyFileWithErrorHandling(String source, String target)
            throws IOException {

        // 파일 존재 여부 확인
        File sourceFile = new File(source);
        if (!sourceFile.exists()) {
            throw new FileNotFoundException("소스 파일이 존재하지 않습니다: " + source);
        }

        if (!sourceFile.canRead()) {
            throw new SecurityException("소스 파일을 읽을 권한이 없습니다: " + source);
        }

        // 대상 파일 디렉토리 확인
        File targetFile = new File(target);
        File targetDir = targetFile.getParentFile();
        if (targetDir != null && !targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new IOException("대상 디렉토리를 생성할 수 없습니다: " + targetDir);
            }
        }

        // 파일 복사 실행
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            System.out.println("파일 복사 완료: " + totalBytes + " 바이트");

        } catch (IOException e) {
            // 복사 실패 시 부분적으로 생성된 파일 삭제
            if (targetFile.exists()) {
                targetFile.delete();
            }
            throw e;
        }
    }
}
```

### 2. 리소스 관리

#### try-with-resources 사용

```java
import java.io.*;

public class ResourceManagementExample {
    public static void main(String[] args) {
        String sourceFile = "source.txt";
        String targetFile = "target.txt";

        // ✅ 권장: try-with-resources 사용
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            // 버퍼의 남은 데이터를 강제로 출력
            bos.flush();

            System.out.println("파일 복사 완료!");

        } catch (IOException e) {
            System.err.println("파일 복사 실패: " + e.getMessage());
        }
        // 자동으로 모든 리소스가 닫힘
    }
}
```

#### 수동 리소스 관리 (권장하지 않음)

```java
import java.io.*;

public class ManualResourceManagementExample {
    public static void main(String[] args) {
        String sourceFile = "source.txt";
        String targetFile = "target.txt";

        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(targetFile);
            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(fos);

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bos.flush();
            System.out.println("파일 복사 완료!");

        } catch (IOException e) {
            System.err.println("파일 복사 실패: " + e.getMessage());
        } finally {
            // ❌ 주의: 역순으로 닫기, null 체크 필요
            try {
                if (bos != null) bos.close();
            } catch (IOException e) {
                System.err.println("BufferedOutputStream 닫기 실패: " + e.getMessage());
            }

            try {
                if (bis != null) bis.close();
            } catch (IOException e) {
                System.err.println("BufferedInputStream 닫기 실패: " + e.getMessage());
            }

            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                System.err.println("FileOutputStream 닫기 실패: " + e.getMessage());
            }

            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
                System.err.println("FileInputStream 닫기 실패: " + e.getMessage());
            }
        }
    }
}
```

## 🚀 성능 최적화 기법

### 1. 버퍼 크기 최적화

#### 버퍼 크기별 성능 테스트

```java
import java.io.*;

public class BufferSizeOptimizationExample {
    public static void main(String[] args) {
        String sourceFile = "large_file.dat";
        String targetFile = "copy_file.dat";

        int[] bufferSizes = {1024, 4096, 8192, 16384, 32768, 65536}; // 1KB ~ 64KB

        System.out.println("버퍼 크기별 성능 테스트");
        System.out.println("========================");

        for (int bufferSize : bufferSizes) {
            long time = testBufferSize(sourceFile, targetFile, bufferSize);
            System.out.printf("버퍼 크기 %6d bytes: %6d ms\n", bufferSize, time);
        }
    }

    private static long testBufferSize(String source, String target, int bufferSize) {
        long startTime = System.currentTimeMillis();

        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(target)) {

            byte[] buffer = new byte[bufferSize];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis() - startTime;
    }
}
```

### 2. 스트림 체이닝 최적화

#### 최적화된 스트림 체이닝

```java
import java.io.*;

public class OptimizedStreamChainingExample {
    public static void main(String[] args) {
        String sourceFile = "source.dat";
        String targetFile = "target.dat";

        try {
            // ✅ 최적화된 순서: 파일 → 버퍼링 → 데이터 처리
            try (InputStream fis = new FileInputStream(sourceFile);
                 InputStream bis = new BufferedInputStream(fis, 32768); // 32KB 버퍼
                 DataInputStream dis = new DataInputStream(bis);

                 OutputStream fos = new FileOutputStream(targetFile);
                 OutputStream bos = new BufferedOutputStream(fos, 32768); // 32KB 버퍼
                 DataOutputStream dos = new DataOutputStream(bos)) {

                // 데이터 읽기 및 쓰기
                int count = dis.readInt();
                dos.writeInt(count);

                for (int i = 0; i < count; i++) {
                    String name = dis.readUTF();
                    int age = dis.readInt();
                    double score = dis.readDouble();

                    dos.writeUTF(name);
                    dos.writeInt(age);
                    dos.writeDouble(score);
                }

                // 버퍼의 남은 데이터를 강제로 출력
                bos.flush();

                System.out.println("데이터 처리 완료!");
            }

        } catch (IOException e) {
            System.err.println("데이터 처리 실패: " + e.getMessage());
        }
    }
}
```

### 3. 메모리 사용량 최적화

#### 메모리 효율적인 파일 처리

```java
import java.io.*;

public class MemoryEfficientFileExample {
    private static final int BUFFER_SIZE = 8192; // 8KB

    public static void main(String[] args) {
        String sourceFile = "very_large_file.dat";
        String targetFile = "efficient_copy.dat";

        try {
            File source = new File(sourceFile);
            long totalSize = source.length();

            System.out.println("메모리 효율적인 파일 복사 시작...");
            System.out.println("전체 크기: " + totalSize + " 바이트");
            System.out.println("버퍼 크기: " + BUFFER_SIZE + " 바이트");

            long startTime = System.currentTimeMillis();

            try (FileInputStream fis = new FileInputStream(source);
                 FileOutputStream fos = new FileOutputStream(targetFile)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long copiedSize = 0;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    copiedSize += bytesRead;

                    // 진행률 표시 (1%마다)
                    if (copiedSize % (totalSize / 100) < BUFFER_SIZE) {
                        double progress = (double) copiedSize / totalSize * 100;
                        System.out.printf("\r진행률: %.1f%% (%d/%d 바이트)",
                            progress, copiedSize, totalSize);
                    }
                }

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                System.out.println("\n파일 복사 완료!");
                System.out.println("소요 시간: " + duration + "ms");
                System.out.printf("처리 속도: %.2f MB/s\n",
                    (totalSize / 1024.0 / 1024.0) / (duration / 1000.0));

            }

        } catch (IOException e) {
            System.err.println("파일 복사 실패: " + e.getMessage());
        }
    }
}
```

## 🔗 실습 연결

이제 Java IO 스트림의 이론을 모두 학습했습니다! 다음 실습을 진행하세요:

- **step03**: 대용량 파일 업로드 처리 (IO 스트림 실습)
- **step04**: 간단한 검색 기능 구현
- **step05**: 리치 텍스트 에디터 통합

## 💡 정리

1. **파일 조작**: 복사, 이동, 삭제 등의 기본 기능을 스트림으로 구현할 수 있습니다.
2. **대용량 파일 처리**: 청크 단위 처리, 병렬 처리, 메모리 매핑 등 다양한 전략을 사용할 수 있습니다.
3. **에러 처리**: 체계적인 예외 처리로 안정적인 프로그램을 만들 수 있습니다.
4. **리소스 관리**: try-with-resources를 사용하여 자동 리소스 관리를 할 수 있습니다.
5. **성능 최적화**: 적절한 버퍼 크기, 스트림 체이닝, 메모리 효율성을 고려하여 최적화할 수 있습니다.

이제 Java IO 스트림의 모든 이론을 마스터했으니, 실습을 통해 실제 활용법을 익혀보세요! 🚀

**다음 단계**: `step03`에서 대용량 파일 업로드 시스템을 구현하여 이론을 실무에 적용해보세요.
