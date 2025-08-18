# 04. Java 고급 스트림 (Advanced Streams)

## 🎯 학습 목표

- `DataInputStream`/`DataOutputStream`으로 기본 타입 데이터 처리 방법 학습
- `ObjectInputStream`/`ObjectOutputStream`으로 객체 직렬화 방법 파악
- `SequenceInputStream`으로 여러 스트림 연결 방법 학습
- 고급 스트림의 실무 활용법 이해

## 📚 DataInputStream / DataOutputStream

### 정의

`DataInputStream`과 `DataOutputStream`은 Java의 기본 데이터 타입(int, double, String 등)을 바이트 스트림으로 변환하여 읽고 쓸 수 있게 해주는 스트림입니다.

### 특징

- **타입 안전성**: 각 데이터 타입에 맞는 메서드 제공
- **플랫폼 독립성**: 모든 Java 플랫폼에서 동일한 형식
- **효율성**: 기본 타입을 최적화된 방식으로 처리

### DataOutputStream 주요 메서드

#### 1. 기본 타입 쓰기

```java
// 정수형
void writeInt(int v)        // 4바이트
void writeLong(long v)      // 8바이트
void writeShort(int v)      // 2바이트
void writeByte(int v)       // 1바이트

// 실수형
void writeFloat(float v)    // 4바이트
void writeDouble(double v)  // 8바이트

// 문자형
void writeChar(int v)       // 2바이트
void writeBoolean(boolean v) // 1바이트

// 문자열
void writeUTF(String str)   // UTF-8 인코딩
```

#### 2. 실습 예제

```java
import java.io.*;

public class DataOutputStreamExample {
    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("data.dat");
             DataOutputStream dos = new DataOutputStream(fos)) {

            // 다양한 타입의 데이터 쓰기
            dos.writeInt(12345);
            dos.writeUTF("안녕하세요");
            dos.writeDouble(3.14159);
            dos.writeBoolean(true);
            dos.writeChar('A');
            dos.writeLong(9876543210L);

            System.out.println("데이터 쓰기 완료!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### DataInputStream 주요 메서드

#### 1. 기본 타입 읽기

```java
// 정수형
int readInt()           // 4바이트
long readLong()         // 8바이트
short readShort()       // 2바이트
byte readByte()         // 1바이트

// 실수형
float readFloat()       // 4바이트
double readDouble()     // 8바이트

// 문자형
char readChar()         // 2바이트
boolean readBoolean()   // 1바이트

// 문자열
String readUTF()        // UTF-8 인코딩
```

#### 2. 실습 예제

```java
import java.io.*;

public class DataInputStreamExample {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("data.dat");
             DataInputStream dis = new DataInputStream(fis)) {

            // 데이터를 쓴 순서와 동일하게 읽기
            int number = dis.readInt();
            String text = dis.readUTF();
            double pi = dis.readDouble();
            boolean flag = dis.readBoolean();
            char letter = dis.readChar();
            long bigNumber = dis.readLong();

            System.out.println("읽은 데이터:");
            System.out.println("정수: " + number);
            System.out.println("문자열: " + text);
            System.out.println("실수: " + pi);
            System.out.println("불린: " + flag);
            System.out.println("문자: " + letter);
            System.out.println("긴 정수: " + bigNumber);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### 3. 학생 정보 관리 예제

```java
import java.io.*;

public class StudentDataExample {
    public static void main(String[] args) {
        // 학생 정보 쓰기
        writeStudentData();

        // 학생 정보 읽기
        readStudentData();
    }

    private static void writeStudentData() {
        try (FileOutputStream fos = new FileOutputStream("students.dat");
             DataOutputStream dos = new DataOutputStream(fos)) {

            // 학생 수 쓰기
            dos.writeInt(3);

            // 첫 번째 학생
            dos.writeUTF("김철수");
            dos.writeInt(20);
            dos.writeDouble(85.5);

            // 두 번째 학생
            dos.writeUTF("이영희");
            dos.writeInt(19);
            dos.writeDouble(92.3);

            // 세 번째 학생
            dos.writeUTF("박민수");
            dos.writeInt(21);
            dos.writeDouble(78.9);

            System.out.println("학생 정보 쓰기 완료!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readStudentData() {
        try (FileInputStream fis = new FileInputStream("students.dat");
             DataInputStream dis = new DataInputStream(fis)) {

            int studentCount = dis.readInt();
            System.out.println("\n학생 수: " + studentCount);

            for (int i = 0; i < studentCount; i++) {
                String name = dis.readUTF();
                int age = dis.readInt();
                double score = dis.readDouble();

                System.out.printf("학생 %d: %s, %d세, 점수: %.1f\n",
                    i + 1, name, age, score);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## 🔄 ObjectInputStream / ObjectOutputStream

### 정의

`ObjectInputStream`과 `ObjectOutputStream`은 Java 객체를 바이트 스트림으로 직렬화(serialization)하고 역직렬화(deserialization)할 수 있게 해주는 스트림입니다.

### 직렬화란?

- **직렬화(Serialization)**: 객체를 바이트 스트림으로 변환
- **역직렬화(Deserialization)**: 바이트 스트림을 객체로 복원
- **용도**: 객체 저장, 네트워크 전송, 캐싱 등

### 직렬화 가능한 클래스 만들기

```java
import java.io.Serializable;

public class Student implements Serializable {
    // 직렬화 버전 ID (선택사항이지만 권장)
    private static final long serialVersionUID = 1L;

    private String name;
    private int age;
    private double score;

    // transient 키워드로 직렬화 제외
    private transient String password;

    // 생성자, getter, setter
    public Student(String name, int age, double score) {
        this.name = name;
        this.age = age;
        this.score = score;
        this.password = "secret";
    }

    // getter, setter 메서드들...

    @Override
    public String toString() {
        return "Student{name='" + name + "', age=" + age + ", score=" + score + "}";
    }
}
```

### ObjectOutputStream 사용법

```java
import java.io.*;

public class ObjectOutputStreamExample {
    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("students.obj");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            // Student 객체들 생성
            Student student1 = new Student("김철수", 20, 85.5);
            Student student2 = new Student("이영희", 19, 92.3);
            Student student3 = new Student("박민수", 21, 78.9);

            // 객체 직렬화하여 쓰기
            oos.writeObject(student1);
            oos.writeObject(student2);
            oos.writeObject(student3);

            System.out.println("객체 직렬화 완료!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### ObjectInputStream 사용법

```java
import java.io.*;

public class ObjectInputStreamExample {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("students.obj");
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            System.out.println("직렬화된 객체 읽기:");

            // 객체 역직렬화하여 읽기
            Student student1 = (Student) ois.readObject();
            Student student2 = (Student) ois.readObject();
            Student student3 = (Student) ois.readObject();

            System.out.println("학생 1: " + student1);
            System.out.println("학생 2: " + student2);
            System.out.println("학생 3: " + student3);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
```

### 4. 복합 객체 직렬화 예제

```java
import java.io.*;
import java.util.*;

public class ComplexObjectExample {
    public static void main(String[] args) {
        // 복합 객체 생성
        School school = new School("고등학교", "서울시");
        List<Student> students = Arrays.asList(
            new Student("김철수", 20, 85.5),
            new Student("이영희", 19, 92.3),
            new Student("박민수", 21, 78.9)
        );
        school.setStudents(students);

        // 객체 직렬화
        try (FileOutputStream fos = new FileOutputStream("school.obj");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(school);
            System.out.println("학교 객체 직렬화 완료!");

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 객체 역직렬화
        try (FileInputStream fis = new FileInputStream("school.obj");
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            School loadedSchool = (School) ois.readObject();
            System.out.println("\n역직렬화된 학교 정보:");
            System.out.println(loadedSchool);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class School implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String location;
    private List<Student> students;

    public School(String name, String location) {
        this.name = name;
        this.location = location;
    }

    // getter, setter 메서드들...

    @Override
    public String toString() {
        return "School{name='" + name + "', location='" + location +
               "', students=" + students + "}";
    }
}
```

## 🔗 SequenceInputStream

### 정의

`SequenceInputStream`은 여러 개의 입력 스트림을 하나의 입력 스트림으로 연결하는 스트림입니다.

### 특징

- **순차적 읽기**: 여러 파일을 하나의 파일처럼 읽기
- **자동 전환**: 첫 번째 스트림이 끝나면 자동으로 다음 스트림으로 전환
- **리소스 관리**: 모든 스트림을 자동으로 닫기

### 생성자

```java
// 두 개의 스트림 연결
InputStream is1 = new FileInputStream("file1.txt");
InputStream is2 = new FileInputStream("file2.txt");
SequenceInputStream sis = new SequenceInputStream(is1, is2);

// 여러 스트림을 Enumeration으로 연결
Vector<InputStream> streams = new Vector<>();
streams.add(new FileInputStream("file1.txt"));
streams.add(new FileInputStream("file2.txt"));
streams.add(new FileInputStream("file3.txt"));
SequenceInputStream sis = new SequenceInputStream(streams.elements());
```

### 실습 예제

#### 1. 기본 사용법

```java
import java.io.*;

public class SequenceInputStreamExample {
    public static void main(String[] args) {
        try (FileInputStream fis1 = new FileInputStream("part1.txt");
             FileInputStream fis2 = new FileInputStream("part2.txt");
             SequenceInputStream sis = new SequenceInputStream(fis1, fis2)) {

            // 두 파일을 하나로 읽기
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = sis.read(buffer)) != -1) {
                System.out.write(buffer, 0, bytesRead);
            }

            System.out.println("\n파일 연결 읽기 완료!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### 2. 여러 파일 연결 예제

```java
import java.io.*;
import java.util.*;

public class MultipleFileExample {
    public static void main(String[] args) {
        try {
            // 여러 파일 스트림 생성
            List<InputStream> streams = new ArrayList<>();
            streams.add(new FileInputStream("header.txt"));
            streams.add(new FileInputStream("content.txt"));
            streams.add(new FileInputStream("footer.txt"));

            // Vector로 변환 (Enumeration을 위해)
            Vector<InputStream> vector = new Vector<>(streams);

            // SequenceInputStream으로 연결
            try (SequenceInputStream sis = new SequenceInputStream(vector.elements())) {

                // 연결된 파일들을 하나의 파일로 저장
                try (FileOutputStream fos = new FileOutputStream("combined.txt")) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = sis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }

                System.out.println("여러 파일 연결 완료!");
            }

            // 개별 스트림들 닫기
            for (InputStream is : streams) {
                is.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### 3. 로그 파일 병합 예제

```java
import java.io.*;
import java.util.*;

public class LogMergeExample {
    public static void main(String[] args) {
        try {
            // 로그 파일들을 날짜순으로 정렬
            String[] logFiles = {
                "log_2024_01_01.txt",
                "log_2024_01_02.txt",
                "log_2024_01_03.txt"
            };

            // 각 로그 파일의 존재 여부 확인
            List<InputStream> validStreams = new ArrayList<>();
            for (String logFile : logFiles) {
                File file = new File(logFile);
                if (file.exists()) {
                    validStreams.add(new FileInputStream(file));
                }
            }

            if (validStreams.isEmpty()) {
                System.out.println("병합할 로그 파일이 없습니다.");
                return;
            }

            // Vector로 변환
            Vector<InputStream> vector = new Vector<>(validStreams);

            // 로그 파일들 병합
            try (SequenceInputStream sis = new SequenceInputStream(vector.elements());
                 FileOutputStream fos = new FileOutputStream("merged_log.txt")) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = sis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                System.out.println("로그 파일 병합 완료! 총 " + totalBytes + " 바이트");
            }

            // 개별 스트림들 닫기
            for (InputStream is : validStreams) {
                is.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## ⚠️ 주의사항

### 1. 직렬화 관련 주의점

```java
// ✅ 직렬화 가능한 클래스
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name; // 직렬화됨
    private transient String password; // 직렬화 제외
}

// ❌ 직렬화 불가능한 클래스
public class Student { // Serializable 구현 안함
    private String name;
}
```

### 2. 스트림 순서

```java
// ✅ 올바른 순서
InputStream fis1 = new FileInputStream("file1.txt");
InputStream fis2 = new FileInputStream("file2.txt");
SequenceInputStream sis = new SequenceInputStream(fis1, fis2);

// ❌ 잘못된 순서 (컴파일 에러)
SequenceInputStream sis = new SequenceInputStream(fis1, fis2);
InputStream fis1 = new FileInputStream("file1.txt");
```

### 3. 예외 처리

```java
// ✅ 권장: try-with-resources 사용
try (FileInputStream fis1 = new FileInputStream("file1.txt");
     FileInputStream fis2 = new FileInputStream("file2.txt");
     SequenceInputStream sis = new SequenceInputStream(fis1, fis2)) {
    // 스트림 사용
} catch (IOException e) {
    e.printStackTrace();
}

// ❌ 주의: 수동 리소스 관리
InputStream fis1 = null;
InputStream fis2 = null;
SequenceInputStream sis = null;
try {
    fis1 = new FileInputStream("file1.txt");
    fis2 = new FileInputStream("file2.txt");
    sis = new SequenceInputStream(fis1, fis2);
    // 스트림 사용
} catch (IOException e) {
    e.printStackTrace();
} finally {
    try {
        if (sis != null) sis.close();
        if (fis2 != null) fis2.close();
        if (fis1 != null) fis1.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

## 🔗 다음 강의

**05. 실무 활용** - 파일 복사, 이동, 삭제, 대용량 파일 처리 전략, 에러 처리와 리소스 관리, 성능 최적화 기법을 학습합니다.

- 파일 복사, 이동, 삭제
- 대용량 파일 처리 전략
- 에러 처리와 리소스 관리
- 성능 최적화 기법

## 💡 정리

1. **DataInputStream/DataOutputStream**은 Java 기본 타입을 안전하게 처리합니다.
2. **ObjectInputStream/ObjectOutputStream**은 객체 직렬화를 통해 복잡한 데이터 구조를 저장할 수 있습니다.
3. **SequenceInputStream**은 여러 파일을 하나의 스트림으로 연결하여 순차적으로 읽을 수 있습니다.
4. **직렬화**는 객체를 바이트 스트림으로 변환하여 저장하거나 전송할 수 있게 해줍니다.
5. **transient** 키워드로 직렬화에서 제외할 필드를 지정할 수 있습니다.

이제 고급 스트림의 사용법을 이해했으니, 다음 강의에서 실무 활용법을 학습해보세요! 🚀
