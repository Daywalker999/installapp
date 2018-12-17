# installapp-maven-plugin

This Plugin allows you, to automatically install your jira plugin into an remote Environment.



## Configuration

```xml
            <plugin>
                <groupId>de.daywalker999.installapp</groupId>
                <artifactId>installapp-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <configuration>
                    <username>admin</username>
                    <password>admin</password>
                    <jarName>my-plugin.jar</jarName>
                    <baseUrl>https://myJira.com</baseUrl>
                </configuration>
            </plugin>
```

|Parameter | Description | Required|Default|
|----------|-------------|:---------:|-------|
|username  | Username of Jira admin user | | admin |
|password  | Password of Jira admin user | | admin |
|jarName  | Name of the final jar file| |${artifactId}-${version}.jar|
|baseUrl| URL of the Jira instance|X||

## Usage
From Commandline:
```
mvn de.daywalker999.installapp:installapp-maven-plugin:install
```