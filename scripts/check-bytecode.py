"""Check the class-file version of both packaged runtime artifacts."""

from pathlib import Path
from zipfile import ZipFile


def check_module(module):
    artifacts = [
        path
        for path in Path(module, "target").glob(f"{module}-*.jar")
        if not path.stem.endswith(("-javadoc", "-sources", "-tests"))
    ]
    if len(artifacts) != 1:
        raise SystemExit(f"Expected one runtime JAR for {module}: {artifacts}")
    artifact = artifacts[0]
    with ZipFile(artifact) as jar:
        classes = [entry for entry in jar.namelist() if entry.endswith(".class")]
        if not classes:
            raise SystemExit(f"{artifact}: no classes to verify")
        for entry in classes:
            with jar.open(entry) as class_file:
                header = class_file.read(8)
            if header[:4] != b"\xca\xfe\xba\xbe" or header[6:] != b"\x00\x34":
                raise SystemExit(f"{artifact}:{entry}: expected Java 8 bytecode")
    print(f"{artifact}: Java 8 bytecode verified")


if __name__ == "__main__":
    for module_name in ("brasil4j-core", "brasil4j-kotlin"):
        check_module(module_name)
