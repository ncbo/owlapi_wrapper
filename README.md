# owlapi_wrapper

[![Java Unit Tests](https://github.com/ncbo/owlapi_wrapper/actions/workflows/unit-tests.yml/badge.svg)](https://github.com/ncbo/owlapi_wrapper/actions/workflows/unit-tests.yml)
[![codecov](https://codecov.io/gh/ncbo/owlapi_wrapper/graph/badge.svg?token=HEt3yP8T0i)](https://codecov.io/gh/ncbo/owlapi_wrapper)
[![GitHub Release](https://img.shields.io/github/v/release/ncbo/owlapi_wrapper)](https://github.com/ncbo/owlapi_wrapper/releases)
[![License: BSD 2-Clause](https://img.shields.io/badge/License-BSD%202--Clause-blue.svg)](https://opensource.org/licenses/BSD-2-Clause)

`owlapi_wrapper` is a small Java command-line tool built around the [OWL API](https://github.com/owlcs/owlapi). It parses OWL, RDF(S), SKOS, 
and OBO ontologies and writes a normalized RDF/XML export plus a small metrics report.

The project is used in the [BioPortal](https://bioportal.bioontology.org/) ecosystem to load ontologies and add a few BioPortal-oriented annotations 
during serialization.

## Features

- Parses a master ontology file from a local repository of ontology files
- Writes a serialized RDF/XML output file named `owlapi.xrdf`.
- Writes a `metrics.csv` file with class, individual, property, and max-depth counts.
- Adds some normalization used by BioPortal, e.g., SKOS notation or prefix annotations for classes 

## Usage

The build produces a self-contained ("shaded") jar whose entry point is `OntologyParserCommand`, so it can be
run directly:

```shell
java -jar owlapi-wrapper-1.5.1.jar \
  --input-repository /path/to/input/repo \
  --master-filename pizza.owl \
  --output-repository /path/to/output/repo \
  --reasoner true
```

| Option | Long form            | Description                                                                                                       |
|--------|----------------------|-------------------------------------------------------------------------------------------------------------------|
| `-i`   | `--input-repository` | Folder holding the ontology files to parse. Imports are resolved against the files in this folder.                  |
| `-m`   | `--master-filename`  | The ontology file to load first. Interpreted relative to `-i`; if `-i` is omitted, it is a path to a single file.    |
| `-o`   | `--output-repository`| Folder to write results to. Created if it does not exist.                                                           |
| `-r`   | `--reasoner`         | Whether to run the reasoner. Defaults to `true`; pass `false` to skip it.                                           |

On success the output folder contains `owlapi.xrdf` (the normalized RDF/XML export) and `metrics.csv`. If the
parse produces errors, they are written to `errors.log` in the same folder. The command exits with a non-zero
status when the invocation is invalid or parsing fails.

`src/test/resources/repo/input` contains small example repositories — e.g. `-i src/test/resources/repo/input/pizza
-m pizza.owl` is a quick way to try the tool.

## Building and releasing

Standard Maven commands apply (`mvn test`, `mvn clean package`); the project targets Java 11. The version is
carried by the `revision` property rather than a literal `<version>`, so a local build produces
`target/owlapi-wrapper-<revision>.jar` and can be overridden with `mvn clean package -Drevision=1.5.1`.

Releasing is automated: publishing a GitHub Release with a tag of the form `vX.Y.Z` triggers the
[release workflow](.github/workflows/release.yml), which builds the jar at that version and attaches it to the
release. The tag is the sole source of truth — `revision` stays at a `-SNAPSHOT` value and is not expected to
match the latest release. The workflow rejects tags with pre-release or build suffixes.

## License

The 2-Clause BSD License. See [LICENSE.md](LICENSE.md) for more information.

## Contributors

<a href="https://github.com/ncbo/owlapi_wrapper/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=ncbo/owlapi_wrapper" />
</a>
