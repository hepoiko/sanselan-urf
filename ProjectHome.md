Implementation for image/urf

Thanks to Jim Covery for his initial decode information

https://github.com/AlanQuatermain/unirast

To use this library in Maven, you must add following into your pom.xml

Repository setting
```
<repositories>
  <repository>
    <id>sanselan-urf-repo-snapshot</id>
    <url>http://sanselan-urf.googlecode.com/svn/maven/snapshots/</url>
  </repository>
   <repository>
    <id>sanselan-urf-repo-release</id>
    <url>http://sanselan-urf.googlecode.com/svn/maven/release/</url>
  </repository>
</repositories>
```

Dependence setting
```
<dependence>
  <groupId>org.apache.sanselan</groupId>
  <artifactId>sanselan-urf</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependence>
```