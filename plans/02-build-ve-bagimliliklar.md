# 02 — Build ve Bağımlılıklar

## Java Sürümü: 21 (zorunlu)

MCP Server için kullanılan **Spring AI 2.0**, derleme için **Java 21** gerektirir
(Spring AI 2.0, Spring Boot 4 + Spring Framework 7 + Jakarta EE 11 tabanlıdır).
Bu yüzden `pom.xml` içinde:

```xml
<properties>
    <java.version>21</java.version>
    <spring-ai.version>2.0.0</spring-ai.version>
</properties>
```

> Not: Spring Boot 4'ün kendi tabanı Java 17 olsa da, Spring AI 2.0 yüzünden proje 21'e
> çekilmiştir. Java 21 kurulu değilse `release version 21 not supported` hatası alınır.

## Bağımlılık Yönetimi (BOM)

Spring AI sürümünü tek yerden yönetmek için `spring-ai-bom` import edilir:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## Eklenen Bağımlılıklar

| Bağımlılık | Amaç |
|-----------|------|
| `spring-boot-starter-data-jpa` | JPA / repository katmanı |
| `com.h2database:h2` (runtime) | In-memory veritabanı |
| `spring-ai-starter-mcp-server-webmvc` | MCP Server (SSE transport) |
| `net.datafaker:datafaker` | Rastgele örnek veri |
| `lombok` | Boilerplate azaltma (zaten vardı) |

## Paketleme

WAR paketleme korunur (`<packaging>war</packaging>`). Geliştirmede gömülü Tomcat ile
`./mvnw spring-boot:run` çalışır.

## Sürüm Doğrulama Notu

`spring-ai-bom:2.0.0` Maven Central'da yayınlanmıştır; ayrıca bir milestone repository
gerekmez. İleride sürüm değişirse yalnızca `spring-ai.version` property'si güncellenir.
