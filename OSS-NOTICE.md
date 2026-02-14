# MCFootball — Open-Source Dependency Notice

This project uses the following third-party libraries:

| Library | Version | License | URL |
|---------|---------|---------|-----|
| MontiCore Runtime | 7.7.0-SNAPSHOT | BSD 3-Clause | https://github.com/MontiCore/monticore |
| MontiCore Grammar | 7.7.0-SNAPSHOT | BSD 3-Clause | https://github.com/MontiCore/monticore |
| SE-Commons Logging | 7.7.0-SNAPSHOT | BSD 3-Clause | https://github.com/MontiCore/se-commons |
| SE-Commons Utilities | 7.7.0-SNAPSHOT | BSD 3-Clause | https://github.com/MontiCore/se-commons |
| FreeMarker | 2.3.32 | Apache-2.0 | https://freemarker.apache.org/ |
| JUnit | 4.13.2 (test only) | EPL-2.0 | https://junit.org/junit4/ |
| JaCoCo | 0.8.11 (test only) | EPL-2.0 | https://www.jacoco.org/ |

## License Compatibility

- **BSD 3-Clause** (MontiCore, SE-Commons): Permissive. Compatible with most open-source and commercial licenses.
- **Apache-2.0** (FreeMarker): Permissive. Compatible with BSD and most open-source licenses.
- **EPL-2.0** (JUnit, JaCoCo): Test-only dependencies, not distributed with the application. No license conflict.

## Risk Assessment

- **No copyleft (GPL) dependencies** in runtime or compile classpath.
- **SNAPSHOT versions**: MontiCore 7.7.0-SNAPSHOT and SE-Commons 7.7.0-SNAPSHOT are pre-release builds from the RWTH Aachen nexus. For production deployment, consider pinning to a stable release once available.
- **No known license conflicts** with the project's intended use.

---

*Generated: 2026-02-14*
