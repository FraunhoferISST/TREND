<div align="center">
  <picture>
    <source width="340" media="(prefers-color-scheme: dark)" srcset="docs/static/img/branding/logo-sub/white/trend_logo-sub_w.svg">
    <source width="340" media="(prefers-color-scheme: light)" srcset="docs/static/img/branding/logo-sub/black/trend_logo-sub_b.svg">
    <img width="340" alt="TREND (Traceability Enforcement of Datatransfers) logo" src="docs/static/img/branding/logo-sub/black/trend_logo-sub_b.svg">
  </picture>
  <br />
  <br />
  <img alt="GitHub Issues" src="https://img.shields.io/github/issues/FraunhoferISST/TREND">
  <img alt="GitHub Pull Requests" src="https://img.shields.io/github/issues-pr/FraunhoferISST/TREND">
  <img alt="GitHub repo size" src="https://img.shields.io/github/repo-size/FraunhoferISST/TREND">
  <img alt="GitHub commit activity]" src="https://img.shields.io/github/commit-activity/t/FraunhoferISST/TREND">
  <a href="https://fraunhoferisst.github.io/TREND/">
    <img alt="Documentation" src="https://img.shields.io/badge/docs-online-green">
  </a>
  <img alt="GitHub Repo stars" src="https://img.shields.io/github/stars/FraunhoferISST/TREND">
</div>

## Table of Contents

- [About](#about)
- [Documentation](#documentation)
- [Structure](#structure)
- [Getting Started](#getting-started)
    - [System Prerequisites](#system-prerequisites)
    - [Quick Start](#quick-start)
- [Contributing](#contributing)
- [License](#license)
- [Developers](#developers)

## About

The *Traceability Enforcement of Datatransfers* (TREND) project aims to address some of the current
challenges in implementing data sovereignty solutions on a broader scale. The objective is to use
state-of-the-art digital watermarking techniques to embed metadata securely in the data being
exchanged, along with dedicated protocol-level checks for validation and enforcement. This enables
system-independent sovereignty checks to secure the data assets of the data owner without privacy
sacrifices.

This repository includes a generic **steganography / watermarking** library to hide any byte 
encoded data in a cover text including two usage examples of a webinterface and command line 
tool. The following example shows how the webinterface includes the watermark "Fraunhofer ISST" 
inside a Lorem ipsum dummy cover text and extracts it afterward:
![Animated example of the webinterface](docs/static/img/webinterface-demo.gif)

> [!NOTE]
> There is a pending German patent application with the application number 10 2023 125 012.4. In
> order to use the TREND watermarker Software in the form published here, a patent license is
> required in addition to the license for the Software. See `LICENSE` for more information. In
> case of any questions or uncertainties, please contact us at trend@isst.fraunhofer.de.

## Documentation

All information from usage until development are collected and provided in our
[documentation](https://fraunhoferisst.github.io/TREND/).

## Structure

This project uses a [monolithic repository approach](https://en.wikipedia.org/wiki/Monorepo) and
consists of different parts, located in different subfolders. The hearth is a **watermarker
library**, located in the `watermarker` folder, used by other components like a CLI
tool or a webinterface shipped with this repo. Every part has its own `README` file to get further
information.

**Subfolder overview:**

- **cli**: A command line tool to enable watermarking directly via a shell
- **docs**: The documentation based on Docusaurus
- **samples**: Different examples of watermarked and non-watermarked files
- **watermarker**: The main part of the repository, consisting of a watermarker library to be able
  to watermark (for example) text files
- **webinterface**: A frontend / GUI to use the watermarking inside a browser

## Getting Started

Detailed getting started guides are described for every component in their dedicated `README`
file, located in the corresponding subfolders. In the following, an easy start of the webinterface
with the watermarker library is described.

### System Prerequisites

The following things are needed to run this application:

- docker & docker-compose

### Quick Start

To run the webinterface, just clone the repo locally and run the `docker-compose.yml` file in the
root directory of the project:

```shell
$ git clone https://github.com/FraunhoferISST/TREND.git
$ cd TREND
$ docker-compose up
```

After the startup finished, try to visit the webinterface at http://localhost:8080

## Contributing

Contributions to this project are greatly appreciated! Every contribution needs to accept the
Corporate Contributor License Agreement, located in the `CLA.md` file. For more details, see the
`CONTRIBUTING.md` file.

## License

This work is licensed under the Fraunhofer License (on the basis of the MIT license). See
`LICENSE` file for more information.

The initial project version was created within the scope of
the [Center of Excellence Logistics and It](https://ce-logit.com/).

## Developers

- Malte Hellmeier ([Fraunhofer ISST](https://www.isst.fraunhofer.de/en.html))
- Haydar Qarawlus ([Fraunhofer ISST](https://www.isst.fraunhofer.de/en.html))
- Hendrik Norkowski ([Fraunhofer ISST](https://www.isst.fraunhofer.de/en.html))
- Ernst-Christoph Schrewe ([Fraunhofer ISST](https://www.isst.fraunhofer.de/en.html))
