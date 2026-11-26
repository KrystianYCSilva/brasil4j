"""Compare packaged Java and Kotlin APIs against a built baseline with japicmp."""

from argparse import ArgumentParser
import os
from pathlib import Path
import subprocess
from xml.etree import ElementTree

MAVEN = {"m": "http://maven.apache.org/POM/4.0.0"}


def artifact(root, module, version):
    return root / module / "target" / f"{module}-{version}.jar"


def kotlin_stdlib(root, repository):
    version = property_value(root, "properties/kotlin.version")
    return repository / "org" / "jetbrains" / "kotlin" / "kotlin-stdlib" / version / f"kotlin-stdlib-{version}.jar"


def property_value(root, path):
    qualified_path = "/".join(f"m:{part}" for part in path.split("/"))
    value = ElementTree.parse(root / "pom.xml").findtext(qualified_path, namespaces=MAVEN)
    if not value:
        raise SystemExit(f"{root / 'pom.xml'}: missing {path}")
    return value.strip()


def compare(java, tool, old, new, old_cp=(), new_cp=()):
    for file in (tool, old, new, *old_cp, *new_cp):
        if not file.is_file():
            raise SystemExit(f"Required JAR is missing: {file}")

    command = [
        java, "-jar", str(tool), "--old", str(old), "--new", str(new),
        "-a", "protected", "--include-synthetic", "--only-modified",
        "--error-on-binary-incompatibility", "--error-on-source-incompatibility",
    ]
    if old_cp:
        command += [
            "--old-classpath", os.pathsep.join(map(str, old_cp)),
            "--new-classpath", os.pathsep.join(map(str, new_cp)),
        ]
    result = subprocess.run(command)
    if result.returncode:
        raise SystemExit(result.returncode)


def main():
    parser = ArgumentParser(description=__doc__)
    parser.add_argument("baseline", type=Path)
    parser.add_argument("candidate", type=Path)
    parser.add_argument("japicmp", type=Path)
    parser.add_argument("maven_repository", type=Path)
    args = parser.parse_args()

    old_version = property_value(args.baseline, "version")
    new_version = property_value(args.candidate, "version")
    java_home = os.environ.get("JAVA_HOME")
    java = str(Path(java_home) / "bin" / "java") if java_home else "java"
    old_core = artifact(args.baseline, "brasil4j-core", old_version)
    new_core = artifact(args.candidate, "brasil4j-core", new_version)
    compare(java, args.japicmp, old_core, new_core)
    compare(
        java, args.japicmp,
        artifact(args.baseline, "brasil4j-kotlin", old_version),
        artifact(args.candidate, "brasil4j-kotlin", new_version),
        (old_core, kotlin_stdlib(args.baseline, args.maven_repository)),
        (new_core, kotlin_stdlib(args.candidate, args.maven_repository)),
    )


if __name__ == "__main__":
    main()
