# 02. Java 입력 스트림 (Input Streams)

## 🎯 학습 목표

- `InputStream`의 기본 메서드 이해
- `FileInputStream`으로 파일 읽기 방법 학습
- `BufferedInputStream`으로 성능 최적화 방법 파악
- `ByteArrayInputStream`으로 메모리 데이터 읽기 방법 학습

## 📚 InputStream 기본 개념

### 정의

`InputStream`은 바이트 데이터를 읽기 위한 추상 클래스입니다. 모든 입력 스트림의 부모 클래스 역할을 합니다.

### 주요 메서드

#### 1. `read()` - 단일 바이트 읽기

```java
public abstract int read() throws IOException
```

- **반환값**: 읽은 바이트 (0~255) 또는 -1 (파일 끝)
- **용도**: 1바이트씩 순차적으로 읽기

```java
InputStream is = new FileInputStream("file.txt");
int data;
while ((data = is.read()) != -1) {
    System.out.print((char) data); // 바이트를 문자로 변환
}
```

#### 2. `read(byte[] b)` - 바이트 배열로 읽기

```java
public int read(byte[] b) throws IOException
```

- **반환값**: 읽은 바이트 수 또는 -1 (파일 끝)
- **용도**: 여러 바이트를 한 번에 읽어 성능 향상

```java
InputStream is = new FileInputStream("file.txt");
byte[] buffer = new byte[1024]; // 1KB 버퍼
int bytesRead;
while ((bytesRead = is.read(buffer)) != -1) {
    // buffer[0] ~ buffer[bytesRead-1]까지 유효한 데이터
    System.out.write(buffer, 0, bytesRead);
}
```

#### 3. `read(byte[] b, int off, int len)` - 부분 읽기

```java
public int read(byte[] b, int off, int len) throws IOException
```

- **매개변수**:
  - `b`: 데이터를 저장할 바이트 배열
  - `off`: 배열의 시작 위치
  - `len`: 읽을 바이트 수
- **용도**: 배열의 특정 위치부터 지정된 크기만큼 읽기

```java
InputStream is = new FileInputStream("file.txt");
byte[] buffer = new byte[1024];
int bytesRead = is.read(buffer, 10, 100); // 10번째 위치부터 100바이트 읽기
```

## 🗂️ FileInputStream

### 정의

파일에서 바이트 데이터를 읽기 위한 입력 스트림입니다.

### 생성자

```java
// 파일 경로로 생성
FileInputStream fis1 = new FileInputStream("file.txt");

// File 객체로 생성
File file = new File("file.txt");
FileInputStream fis2 = new FileInputStream(file);

// 파일 디스크립터로 생성 (고급)
FileDescriptor fd = ...;
FileInputStream fis3 = new FileInputStream(fd);
```

### 실습 예제

#### 1. 기본 파일 읽기

```java
import java.io.*;

public class FileInputStreamExample {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("sample.txt")) {
            int data;
            System.out.println("파일 내용:");
            while ((data = fis.read()) != -1) {
                System.out.print((char) data);
            }
        } catch (IOException e) {
            System.err.println("파일 읽기 오류: " + e.getMessage());
        }
    }
}
```

#### 2. 버퍼를 사용한 효율적인 읽기

```java
import java.io.*;

public class BufferedFileReadExample {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("large_file.txt")) {
            byte[] buffer = new byte[8192]; // 8KB 버퍼
            int bytesRead;
            int totalBytes = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                totalBytes += bytesRead;
                // buffer의 데이터 처리
                processData(buffer, bytesRead);
            }

            System.out.println("총 읽은 바이트: " + totalBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processData(byte[] buffer, int bytesRead) {
        // 실제 데이터 처리 로직
        System.out.println("처리된 바이트: " + bytesRead);
    }
}
```

#### 3. 파일 정보 확인

```java
import java.io.*;

public class FileInfoExample {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("document.pdf")) {
            // 사용 가능한 바이트 수 확인
            int available = fis.available();
            System.out.println("파일 크기: " + available + " 바이트");

            // 파일의 처음 10바이트만 읽기
            byte[] header = new byte[10];
            int read = fis.read(header);
            System.out.println("읽은 바이트: " + read);

            // 헤더 정보 출력
            System.out.print("파일 헤더: ");
            for (byte b : header) {
                System.out.printf("%02X ", b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## 🚀 BufferedInputStream

### 정의

`BufferedInputStream`은 내부 버퍼를 사용하여 읽기 성능을 향상시키는 입력 스트림입니다.

### 장점

- **성능 향상**: 작은 읽기 작업을 모아서 한 번에 처리
- **시스템 호출 감소**: OS 레벨의 읽기 호출 횟수 감소
- **자동 버퍼링**: 내부적으로 최적화된 버퍼 크기 사용

### 생성자

```java
// 기본 버퍼 크기 (보통 8KB)
BufferedInputStream bis1 = new BufferedInputStream(new FileInputStream("file.txt"));

// 사용자 정의 버퍼 크기
BufferedInputStream bis2 = new BufferedInputStream(
    new FileInputStream("file.txt"), 16384); // 16KB
```

### 실습 예제

#### 1. 기본 사용법

```java
import java.io.*;

public class BufferedInputStreamExample {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("large_file.txt");
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int data;
            long startTime = System.currentTimeMillis();

            while ((data = bis.read()) != -1) {
                // 데이터 처리
            }

            long endTime = System.currentTimeMillis();
            System.out.println("읽기 시간: " + (endTime - startTime) + "ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### 2. 성능 비교

```java
import java.io.*;

public class PerformanceComparison {
    public static void main(String[] args) {
        String filename = "large_file.txt";

        // 1. FileInputStream만 사용
        long time1 = measureReadTime(new FileInputStream(filename));
        System.out.println("FileInputStream만 사용: " + time1 + "ms");

        // 2. BufferedInputStream 사용
        long time2 = measureReadTime(new BufferedInputStream(new FileInputStream(filename)));
        System.out.println("BufferedInputStream 사용: " + time2 + "ms");

        // 3. 성능 향상률
        double improvement = ((double)(time1 - time2) / time1) * 100;
        System.out.printf("성능 향상: %.1f%%\n", improvement);
    }

    private static long measureReadTime(InputStream is) {
        long startTime = System.currentTimeMillis();

        try (InputStream stream = is) {
            byte[] buffer = new byte[1024];
            while (stream.read(buffer) != -1) {
                // 읽기만 수행
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis() - startTime;
    }
}
```

#### 3. 마크/리셋 기능

```java
import java.io.*;

public class MarkResetExample {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("data.txt");
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            // 마크 설정 (최대 1000바이트까지 되돌아갈 수 있음)
            if (bis.markSupported()) {
                bis.mark(1000);

                // 데이터 읽기
                byte[] buffer = new byte[100];
                int read = bis.read(buffer);
                System.out.println("첫 번째 읽기: " + read + " 바이트");

                // 마크 위치로 되돌아가기
                bis.reset();

                // 다시 읽기
                read = bis.read(buffer);
                System.out.println("두 번째 읽기: " + read + " 바이트");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## 💾 ByteArrayInputStream

### 정의

바이트 배열에서 데이터를 읽기 위한 입력 스트림입니다.

### 특징

- **메모리 기반**: 파일 I/O 없이 메모리에서 직접 읽기
- **빠른 속도**: 디스크 접근이 없어 매우 빠름
- **테스트 용이**: 단위 테스트에서 가짜 데이터 생성 시 유용

### 생성자

```java
// 바이트 배열로 생성
byte[] data = "Hello, World!".getBytes();
ByteArrayInputStream bais1 = new ByteArrayInputStream(data);

// 배열의 일부만 사용
ByteArrayInputStream bais2 = new ByteArrayInputStream(data, 0, 5); // "Hello"만
```

### 실습 예제

#### 1. 기본 사용법

```java
import java.io.*;

public class ByteArrayInputStreamExample {
    public static void main(String[] args) {
        // 문자열을 바이트 배열로 변환
        String text = "안녕하세요, Java IO 스트림입니다!";
        byte[] bytes = text.getBytes("UTF-8");

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            int data;
            System.out.println("바이트 배열 내용:");
            while ((data = bais.read()) != -1) {
                System.out.printf("%02X ", data); // 16진수로 출력
            }
            System.out.println();

            // 다시 읽기 위해 새로운 스트림 생성
            ByteArrayInputStream bais2 = new ByteArrayInputStream(bytes);
            byte[] buffer = new byte[1024];
            int read = bais2.read(buffer);

            String result = new String(buffer, 0, read, "UTF-8");
            System.out.println("읽은 내용: " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### 2. 데이터 변환 예제

```java
import java.io.*;

public class DataConversionExample {
    public static void main(String[] args) {
        // 다양한 데이터 타입을 바이트 배열로 변환
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            // 데이터 쓰기
            dos.writeInt(12345);
            dos.writeUTF("Hello");
            dos.writeDouble(3.14159);

            byte[] data = baos.toByteArray();

            // ByteArrayInputStream으로 읽기
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 DataInputStream dis = new DataInputStream(bais)) {

                // 데이터 읽기
                int number = dis.readInt();
                String text = dis.readUTF();
                double pi = dis.readDouble();

                System.out.println("읽은 데이터:");
                System.out.println("정수: " + number);
                System.out.println("문자열: " + text);
                System.out.println("실수: " + pi);
            }

        } catch (IOException e) {
            e.printStackTrace();
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

public class StreamChainingExample {
    public static void main(String[] args) {
        try {
            // 파일 → 버퍼링 → 데이터 처리 순서로 연결
            InputStream fileStream = new FileInputStream("data.dat");
            InputStream bufferedStream = new BufferedInputStream(fileStream, 8192);
            DataInputStream dataStream = new DataInputStream(bufferedStream);

            // 데이터 읽기
            int count = dataStream.readInt();
            System.out.println("데이터 개수: " + count);

            for (int i = 0; i < count; i++) {
                String name = dataStream.readUTF();
                int age = dataStream.readInt();
                System.out.printf("이름: %s, 나이: %d\n", name, age);
            }

            // 리소스 정리 (역순으로)
            dataStream.close();
            bufferedStream.close();
            fileStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## ⚠️ 주의사항

### 1. 버퍼 크기 선택

- **너무 작음**: 버퍼링 효과 없음
- **너무 큼**: 메모리 낭비
- **권장**: 8KB (8192바이트) ~ 64KB

### 2. 마크/리셋 제한

```java
BufferedInputStream bis = new BufferedInputStream(fis);
bis.mark(1000); // 최대 1000바이트까지만 되돌아갈 수 있음

// 1000바이트 이상 읽은 후 reset() 호출 시 IOException 발생
```

### 3. 스트림 순서

```java
// ✅ 올바른 순서
InputStream fis = new FileInputStream("file.txt");
InputStream bis = new BufferedInputStream(fis);
InputStream dis = new DataInputStream(bis);

// ❌ 잘못된 순서 (성능 저하)
InputStream bis = new BufferedInputStream(fis);
InputStream fis = new FileInputStream("file.txt"); // 컴파일 에러
```

## 🔗 다음 강의

**03. 출력 스트림** - `OutputStream`과 관련 클래스들의 구체적인 사용법을 학습합니다.

- `FileOutputStream`: 파일에 데이터 쓰기
- `BufferedOutputStream`: 성능 최적화
- `ByteArrayOutputStream`: 메모리 데이터 쓰기

## 💡 정리

1. **FileInputStream**은 파일에서 바이트 단위로 데이터를 읽습니다.
2. **BufferedInputStream**은 버퍼링으로 읽기 성능을 크게 향상시킵니다.
3. **ByteArrayInputStream**은 메모리의 바이트 배열에서 데이터를 읽습니다.
4. **스트림 체이닝**으로 여러 기능을 조합하여 사용할 수 있습니다.

이제 입력 스트림의 사용법을 이해했으니, 다음 강의에서 출력 스트림을 학습해보세요! 🚀
