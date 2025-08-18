# 03. Java 출력 스트림 (Output Streams)

## 🎯 학습 목표

- `OutputStream`의 기본 메서드 이해
- `FileOutputStream`으로 파일 쓰기 방법 학습
- `BufferedOutputStream`으로 성능 최적화 방법 파악
- `ByteArrayOutputStream`으로 메모리 데이터 쓰기 방법 학습

## 📚 OutputStream 기본 개념

### 정의

`OutputStream`은 바이트 데이터를 쓰기 위한 추상 클래스입니다. 모든 출력 스트림의 부모 클래스 역할을 합니다.

### 주요 메서드

#### 1. `write(int b)` - 단일 바이트 쓰기

```java
public abstract void write(int b) throws IOException
```

- **매개변수**: 쓸 바이트 (0~255 범위)
- **용도**: 1바이트씩 순차적으로 쓰기

```java
OutputStream os = new FileOutputStream("file.txt");
os.write(65); // 'A' 문자
os.write(66); // 'B' 문자
os.write(67); // 'C' 문자
```

#### 2. `write(byte[] b)` - 바이트 배열 쓰기

```java
public void write(byte[] b) throws IOException
```

- **매개변수**: 쓸 바이트 배열
- **용도**: 여러 바이트를 한 번에 써서 성능 향상

```java
OutputStream os = new FileOutputStream("file.txt");
byte[] data = "Hello, World!".getBytes();
os.write(data); // 전체 배열을 한 번에 쓰기
```

#### 3. `write(byte[] b, int off, int len)` - 부분 쓰기

```java
public void write(byte[] b, int off, int len) throws IOException
```

- **매개변수**:
  - `b`: 쓸 데이터가 있는 바이트 배열
  - `off`: 배열의 시작 위치
  - `len`: 쓸 바이트 수
- **용도**: 배열의 특정 위치부터 지정된 크기만큼 쓰기

```java
OutputStream os = new FileOutputStream("file.txt");
byte[] data = "Hello, World!".getBytes();
os.write(data, 0, 5); // "Hello"만 쓰기
```

#### 4. `flush()` - 버퍼 비우기

```java
public void flush() throws IOException
```

- **용도**: 버퍼에 남아있는 데이터를 강제로 출력
- **중요성**: 데이터 손실 방지

```java
OutputStream os = new FileOutputStream("file.txt");
os.write("Hello".getBytes());
os.flush(); // 버퍼의 데이터를 즉시 파일에 쓰기
```

## 🗂️ FileOutputStream

### 정의

파일에 바이트 데이터를 쓰기 위한 출력 스트림입니다.

### 생성자

```java
// 파일 경로로 생성 (기존 파일 덮어쓰기)
FileOutputStream fos1 = new FileOutputStream("file.txt");

// File 객체로 생성
File file = new File("file.txt");
FileOutputStream fos2 = new FileOutputStream(file);

// append 모드로 생성 (기존 파일에 추가)
FileOutputStream fos3 = new FileOutputStream("file.txt", true);

// File 객체 + append 모드
FileOutputStream fos4 = new FileOutputStream(file, true);
```

### 실습 예제

#### 1. 기본 파일 쓰기

```java
import java.io.*;

public class FileOutputStreamExample {
    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("sample.txt")) {
            // 문자열을 바이트로 변환하여 쓰기
            String text = "안녕하세요, Java IO 스트림입니다!";
            byte[] bytes = text.getBytes("UTF-8");
            fos.write(bytes);

            System.out.println("파일 쓰기 완료!");
        } catch (IOException e) {
            System.err.println("파일 쓰기 오류: " + e.getMessage());
        }
    }
}
```

#### 2. 바이트 단위로 쓰기

```java
import java.io.*;

public class ByteWriteExample {
    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("numbers.txt")) {
            // 1부터 100까지의 숫자를 바이트로 변환하여 쓰기
            for (int i = 1; i <= 100; i++) {
                fos.write(i);
            }

            System.out.println("숫자 파일 쓰기 완료!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### 3. append 모드 사용

```java
import java.io.*;

public class AppendModeExample {
    public static void main(String[] args) {
        // 첫 번째 실행: 새 파일 생성
        try (FileOutputStream fos = new FileOutputStream("log.txt")) {
            String message = "첫 번째 로그 메시지\n";
            fos.write(message.getBytes());
            System.out.println("첫 번째 로그 작성 완료");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 두 번째 실행: 기존 파일에 추가
        try (FileOutputStream fos = new FileOutputStream("log.txt", true)) {
            String message = "두 번째 로그 메시지\n";
            fos.write(message.getBytes());
            System.out.println("두 번째 로그 추가 완료");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### 4. 파일 복사 예제

```java
import java.io.*;

public class FileCopyExample {
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
            System.err.println("파일 복사 오류: " + e.getMessage());
        }
    }
}
```

## 🚀 BufferedOutputStream

### 정의

`BufferedOutputStream`은 내부 버퍼를 사용하여 쓰기 성능을 향상시키는 출력 스트림입니다.

### 장점

- **성능 향상**: 작은 쓰기 작업을 모아서 한 번에 처리
- **시스템 호출 감소**: OS 레벨의 쓰기 호출 횟수 감소
- **자동 버퍼링**: 내부적으로 최적화된 버퍼 크기 사용

### 생성자

```java
// 기본 버퍼 크기 (보통 8KB)
BufferedOutputStream bos1 = new BufferedOutputStream(new FileOutputStream("file.txt"));

// 사용자 정의 버퍼 크기
BufferedOutputStream bos2 = new BufferedOutputStream(
    new FileOutputStream("file.txt"), 16384); // 16KB
```

### 실습 예제

#### 1. 기본 사용법

```java
import java.io.*;

public class BufferedOutputStreamExample {
    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("large_file.txt");
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            long startTime = System.currentTimeMillis();

            // 100만 바이트 쓰기
            for (int i = 0; i < 1000000; i++) {
                bos.write(i % 256); // 0~255 범위의 값
            }

            // 버퍼의 남은 데이터를 강제로 출력
            bos.flush();

            long endTime = System.currentTimeMillis();
            System.out.println("쓰기 시간: " + (endTime - startTime) + "ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### 2. 성능 비교

```java
import java.io.*;

public class WritePerformanceComparison {
    public static void main(String[] args) {
        String filename1 = "unbuffered.txt";
        String filename2 = "buffered.txt";

        // 1. FileOutputStream만 사용
        long time1 = measureWriteTime(new FileOutputStream(filename1));
        System.out.println("FileOutputStream만 사용: " + time1 + "ms");

        // 2. BufferedOutputStream 사용
        long time2 = measureWriteTime(new BufferedOutputStream(new FileOutputStream(filename2)));
        System.out.println("BufferedOutputStream 사용: " + time2 + "ms");

        // 3. 성능 향상률
        double improvement = ((double)(time1 - time2) / time1) * 100;
        System.out.printf("성능 향상: %.1f%%\n", improvement);
    }

    private static long measureWriteTime(OutputStream os) {
        long startTime = System.currentTimeMillis();

        try (OutputStream stream = os) {
            // 100만 바이트 쓰기
            for (int i = 0; i < 1000000; i++) {
                stream.write(i % 256);
            }
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis() - startTime;
    }
}
```

#### 3. 버퍼 크기별 성능 테스트

```java
import java.io.*;

public class BufferSizeTest {
    public static void main(String[] args) {
        int[] bufferSizes = {1024, 4096, 8192, 16384, 32768}; // 1KB ~ 32KB

        for (int bufferSize : bufferSizes) {
            long time = testBufferSize(bufferSize);
            System.out.printf("버퍼 크기 %5d bytes: %5d ms\n", bufferSize, time);
        }
    }

    private static long testBufferSize(int bufferSize) {
        String filename = "test_" + bufferSize + ".txt";
        long startTime = System.currentTimeMillis();

        try (FileOutputStream fos = new FileOutputStream(filename);
             BufferedOutputStream bos = new BufferedOutputStream(fos, bufferSize)) {

            // 100만 바이트 쓰기
            for (int i = 0; i < 1000000; i++) {
                bos.write(i % 256);
            }
            bos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis() - startTime;
    }
}
```

## 💾 ByteArrayOutputStream

### 정의

바이트 배열에 데이터를 쓰기 위한 출력 스트림입니다.

### 특징

- **메모리 기반**: 파일 I/O 없이 메모리에서 직접 쓰기
- **동적 크기**: 필요에 따라 자동으로 크기 증가
- **데이터 수집**: 여러 소스에서 데이터를 수집할 때 유용

### 주요 메서드

```java
// 바이트 배열로 변환
byte[] toByteArray()

// 문자열로 변환
String toString()

// 문자열로 변환 (인코딩 지정)
String toString(String charsetName)

// 크기 확인
int size()

// 버퍼 내용을 다른 OutputStream에 쓰기
void writeTo(OutputStream out)
```

### 실습 예제

#### 1. 기본 사용법

```java
import java.io.*;

public class ByteArrayOutputStreamExample {
    public static void main(String[] args) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 다양한 데이터 쓰기
            baos.write("Hello, ".getBytes());
            baos.write("World!".getBytes());
            baos.write(33); // 느낌표 문자 '!'

            // 결과 확인
            byte[] result = baos.toByteArray();
            System.out.println("총 바이트 수: " + result.length);
            System.out.println("내용: " + new String(result));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### 2. 데이터 수집 예제

```java
import java.io.*;

public class DataCollectionExample {
    public static void main(String[] args) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 여러 소스에서 데이터 수집
            collectDataFromSource1(baos);
            collectDataFromSource2(baos);
            collectDataFromSource3(baos);

            // 수집된 모든 데이터를 파일에 저장
            try (FileOutputStream fos = new FileOutputStream("collected_data.txt")) {
                baos.writeTo(fos);
            }

            System.out.println("데이터 수집 및 저장 완료!");
            System.out.println("총 수집된 바이트: " + baos.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void collectDataFromSource1(ByteArrayOutputStream baos) throws IOException {
        String data = "소스 1의 데이터\n";
        baos.write(data.getBytes());
    }

    private static void collectDataFromSource2(ByteArrayOutputStream baos) throws IOException {
        String data = "소스 2의 데이터\n";
        baos.write(data.getBytes());
    }

    private static void collectDataFromSource3(ByteArrayOutputStream baos) throws IOException {
        String data = "소스 3의 데이터\n";
        baos.write(data.getBytes());
    }
}
```

#### 3. 이미지 데이터 처리 예제

```java
import java.io.*;

public class ImageDataExample {
    public static void main(String[] args) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 가상의 이미지 헤더 생성
            writeImageHeader(baos);

            // 가상의 이미지 데이터 생성
            writeImageData(baos);

            // 결과를 파일에 저장
            try (FileOutputStream fos = new FileOutputStream("generated_image.raw")) {
                baos.writeTo(fos);
            }

            System.out.println("이미지 데이터 생성 완료!");
            System.out.println("이미지 크기: " + baos.size() + " 바이트");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeImageHeader(ByteArrayOutputStream baos) throws IOException {
        // 간단한 이미지 헤더 (가상)
        baos.write("IMG".getBytes()); // 매직 넘버
        baos.write(0x01); // 버전
        baos.write(0x00); // 플래그
    }

    private static void writeImageData(ByteArrayOutputStream baos) throws IOException {
        // 가상의 이미지 픽셀 데이터 (100x100 = 10,000 픽셀)
        for (int i = 0; i < 10000; i++) {
            baos.write(i % 256); // 픽셀 값
        }
    }
}
```

## 🔗 스트림 체이닝

### 개념

여러 스트림을 연결하여 사용하는 방법입니다. 각 스트림이 특정 기능을 담당합니다.

### 예제

```java
import java.io.*;

public class OutputStreamChainingExample {
    public static void main(String[] args) {
        try {
            // 파일 → 버퍼링 → 데이터 처리 순서로 연결
            OutputStream fileStream = new FileOutputStream("data.dat");
            OutputStream bufferedStream = new BufferedOutputStream(fileStream, 8192);
            DataOutputStream dataStream = new DataOutputStream(bufferedStream);

            // 다양한 타입의 데이터 쓰기
            dataStream.writeInt(100);
            dataStream.writeUTF("Hello, World!");
            dataStream.writeDouble(3.14159);
            dataStream.writeBoolean(true);

            // 버퍼의 데이터를 강제로 출력
            dataStream.flush();

            // 리소스 정리 (역순으로)
            dataStream.close();
            bufferedStream.close();
            fileStream.close();

            System.out.println("데이터 쓰기 완료!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## ⚠️ 주의사항

### 1. flush() 호출 시점

```java
// ✅ 권장: 중요한 데이터 후 즉시 flush
BufferedOutputStream bos = new BufferedOutputStream(fos);
bos.write("중요한 데이터".getBytes());
bos.flush(); // 즉시 출력

// ❌ 주의: flush 없이 close만 하면 데이터 손실 가능
bos.write("데이터".getBytes());
bos.close(); // 버퍼에 남은 데이터가 손실될 수 있음
```

### 2. 버퍼 크기 선택

```java
// ✅ 적절한 버퍼 크기
BufferedOutputStream bos = new BufferedOutputStream(fos, 8192); // 8KB

// ❌ 너무 작은 버퍼
BufferedOutputStream bos = new BufferedOutputStream(fos, 64); // 64바이트

// ❌ 너무 큰 버퍼
BufferedOutputStream bos = new BufferedOutputStream(fos, 1024 * 1024); // 1MB
```

### 3. 예외 처리

```java
// ✅ 권장: try-with-resources 사용
try (FileOutputStream fos = new FileOutputStream("file.txt");
     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
    bos.write("Hello".getBytes());
    bos.flush();
} catch (IOException e) {
    e.printStackTrace();
}

// ❌ 주의: 수동 리소스 관리
FileOutputStream fos = null;
BufferedOutputStream bos = null;
try {
    fos = new FileOutputStream("file.txt");
    bos = new BufferedOutputStream(fos);
    bos.write("Hello".getBytes());
} catch (IOException e) {
    e.printStackTrace();
} finally {
    try {
        if (bos != null) bos.close();
        if (fos != null) fos.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

## 🔗 다음 강의

**04. 고급 스트림** - `DataInputStream`/`DataOutputStream`, `ObjectInputStream`/`ObjectOutputStream` 등 고급 기능을 학습합니다.

- `DataInputStream`/`DataOutputStream`: 기본 타입 데이터 처리
- `ObjectInputStream`/`ObjectOutputStream`: 객체 직렬화
- `SequenceInputStream`: 여러 스트림 연결

## 💡 정리

1. **FileOutputStream**은 파일에 바이트 단위로 데이터를 씁니다.
2. **BufferedOutputStream**은 버퍼링으로 쓰기 성능을 크게 향상시킵니다.
3. **ByteArrayOutputStream**은 메모리의 바이트 배열에 데이터를 씁니다.
4. **flush()**를 적절히 호출하여 데이터 손실을 방지해야 합니다.
5. **스트림 체이닝**으로 여러 기능을 조합하여 사용할 수 있습니다.

이제 출력 스트림의 사용법을 이해했으니, 다음 강의에서 고급 스트림을 학습해보세요! 🚀
