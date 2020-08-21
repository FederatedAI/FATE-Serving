# FATE-Serving

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 
[![CodeStyle](https://img.shields.io/badge/Check%20Style-Google-brightgreen)](https://checkstyle.sourceforge.io/google_style.html) 
[![Style](https://img.shields.io/badge/Check%20Style-Black-black)](https://checkstyle.sourceforge.io/google_style.html) 

## Introduction

FATE-Serving is a high-performance, industrialized serving system for federated learning models, designed for production environments. for more detail, You can click [WIKI](https://github.com/FederatedAI/FATE-Serving/wiki) for more information for more information .

### FATE-Serving now supports

- High performance online Federated Learning algorithms.
- Federated Learning online inference pipeline.
- Dynamic loading federated learning models.
- Can serve multiple models, or multiple versions of the same model.
- Real-time inference using federated learning models.
- Support multi-level cache for remote party federated inference result.
- Support pre-processing, post-processing and data-access adapters for the production deployment.
- Provide service managerment for grpc interface by using zookeeper as registry 
- Requests for publishing models are persisted to local files，so the loaded model will be loaded automatically when the application is restarted





