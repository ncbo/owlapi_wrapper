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

## License

The 2-Clause BSD License. See [LICENSE.md](LICENSE.md) for more information.

## Contributors

<a href="https://github.com/ncbo/owlapi_wrapper/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=ncbo/owlapi_wrapper" />
</a>
