# 08 — Kurulum, Çalıştırma, Test

## Ön Koşul: Java 21

Spring AI 2.0 derleme için **Java 21** gerektirir. Kurulu değilse (macOS / Homebrew):

```bash
brew install openjdk@21
# Homebrew JDK'sı /usr/libexec/java_home'a kayıtlı OLMAYABİLİR; doğrudan prefix kullanın:
export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
java -version   # 21 görünmeli
```

## Build & Test

```bash
./mvnw clean test       # testleri çalıştır
./mvnw clean package    # WAR üret (target/mcpSample-0.0.1-SNAPSHOT.war)
```

## Çalıştırma

```bash
./mvnw spring-boot:run
```

Açılışta seed log'u görünür:
`[SEED] Rastgele veri uretildi -> musteri=20, siparis=..., fatura=..., ticket=...`

## Doğrulama

### 1) REST
```bash
curl localhost:8080/api/customers
curl localhost:8080/api/customers/1/360
```

### 2) H2 Konsolu
Tarayıcı: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:customer360`
- Kullanıcı: `sa`, Şifre: (boş)
- `CRM_CUSTOMER`, `ORDER_RECORD`, `INVOICE`, `TICKET` tablolarında rastgele veriyi görün.

### 3) MCP
- SSE ucu ayakta mı: `http://localhost:8080/sse`
- Bir MCP istemcisi (veya Claude) `listCustomers` ve `getCustomer360` araçlarını çağırabilir
  (bkz. [07 — MCP Server ve Araçlar](07-mcp-server-ve-araclar.md)).

## Testler

| Test | Doğruladığı |
|------|-------------|
| `McpSampleApplicationTests.contextLoads` | Tüm bean'lerle context ayağa kalkıyor |
| `Customer360ServiceTest.listCustomers_...` | Seeder verisi listeleniyor |
| `Customer360ServiceTest.getCustomer360_...` | Dört kaynak birleşiyor, özetler null-güvenli |
| `Customer360ServiceTest.getCustomer360_olmayanMusteri...` | Olmayan müşteride `CustomerNotFoundException` |

## Sorun Giderme

- `release version 21 not supported` → Java 21 kurulu değil veya `JAVA_HOME` 17'yi gösteriyor.
- Spring AI bağımlılığı çözülmüyor → `spring-ai.version` (pom) ve internet erişimini kontrol edin.
