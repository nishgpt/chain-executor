<p align="center"><h1 align="center">CHAIN-EXECUTOR</h1></p>
<p align="center">
	<em><code>❯ A simplified state machine </code></em>
</p>
<p align="center">
	<img src="https://img.shields.io/github/license/nishgpt/chain-executor?style=default&logo=opensourceinitiative&logoColor=white&color=0080ff" alt="license">
	<img src="https://img.shields.io/github/last-commit/nishgpt/chain-executor?style=default&logo=git&logoColor=white&color=0080ff" alt="last-commit">
	<img src="https://img.shields.io/github/languages/top/nishgpt/chain-executor?style=default&color=0080ff" alt="repo-top-language">
	<img src="https://img.shields.io/github/languages/count/nishgpt/chain-executor?style=default&color=0080ff" alt="repo-language-count">
</p>
<p align="center"><!-- default option, no dependency badges. -->
</p>
<p align="center">
	<!-- default option, no dependency badges. -->
</p>
<br>

##  Table of Contents

- [ Overview](#-overview)
- [ Features](#-features)
- [ Project Structure](#-project-structure)
  - [ Project Index](#-project-index)
- [ Getting Started](#-getting-started)
  - [ Prerequisites](#-prerequisites)
  - [ Installation](#-installation)
  - [ Usage](#-usage)
  - [ Testing](#-testing)
- [ Project Roadmap](#-project-roadmap)
- [ Contributing](#-contributing)
- [ License](#-license)
- [ Acknowledgments](#-acknowledgments)

---

##  Overview

A library that lets you define and chain multiple stages in a linear fashion and helps you execute the same against a context. You can register multiple chains against identifiers and execute one as needed.

---

##  Features

|      | Feature         | Summary       |
| :--- | :---:           | :---          |
| ⚙️  | **Architecture**  | <ul><li>Uses a stage-based execution model, where individual processing steps (`StageExecutor` interface,  [src/main/java/com/github/nishgpt/chainexecutor/models/execution/StageExecutor.java](src/main/java/com/github/nishgpt/chainexecutor/models/execution/StageExecutor.java)) are chained together (`StageChain` class, [src/main/java/com/github/nishgpt/chainexecutor/models/stage/StageChain.java](src/main/java/com/github/nishgpt/chainexecutor/models/stage/StageChain.java)).</li><li>`StageExecutionManager` orchestrates the execution of these stages, managing their lifecycle and ensuring sequential execution ([src/main/java/com/github/nishgpt/chainexecutor/StageExecutionManager.java](src/main/java/com/github/nishgpt/chainexecutor/StageExecutionManager.java)).</li><li>Employs a factory pattern (`StageExecutorFactory`, [src/main/java/com/github/nishgpt/chainexecutor/models/execution/StageExecutorFactory.java](src/main/java/com/github/nishgpt/chainexecutor/models/execution/StageExecutorFactory.java)) for creating and managing `StageExecutor` instances, promoting flexibility and extensibility.</li><li>Utilizes custom exceptions (`ChainExecutorException`, [src/main/java/com/github/nishgpt/chainexecutor/exceptions/ChainExecutorException.java](src/main/java/com/github/nishgpt/chainexecutor/exceptions/ChainExecutorException.java)) and error codes (`ErrorCode`, [src/main/java/com/github/nishgpt/chainexecutor/exceptions/ErrorCode.java](src/main/java/com/github/nishgpt/chainexecutor/exceptions/ErrorCode.java)) for robust error handling.</li></ul> |
| 🔩 | **Code Quality**  | <ul><li>Code style and adherence to best practices are inferred from the provided snippets, but cannot be definitively assessed without a full code review.</li><li>The use of interfaces (`StageExecutor`, `StageChainIdentifier`) suggests a focus on modularity and abstraction.</li><li>Custom exception handling improves error management and maintainability.</li><li>Further analysis is needed to assess aspects like code complexity, cyclomatic complexity, and code coverage.</li></ul> |
| 📄 | **Documentation** | <ul><li>Javadoc-style comments are present in the code snippets, indicating some level of inline documentation.</li><li>The provided metadata indicates 14 Java files with documentation in Java (`primary_language='Java' language_counts={'java': 14}`). </li><li>The extent of external documentation (e.g., README, user guides) is unknown.</li><li>More comprehensive documentation would improve understandability and maintainability.</li></ul> |

---

##  Project Structure

```sh
└── chain-executor/
    ├── LICENSE
    ├── README.md
    ├── pom.xml
    └── src
        └── main
```


###  Project Index
<details open>
	<summary><b><code>CHAIN-EXECUTOR/</code></b></summary>
	<details> <!-- __root__ Submodule -->
		<summary><b>__root__</b></summary>
		<blockquote>
			<table>
			</table>
		</blockquote>
	</details>
	<details> <!-- src Submodule -->
		<summary><b>src</b></summary>
		<blockquote>
			<details>
				<summary><b>main</b></summary>
				<blockquote>
					<details>
						<summary><b>java</b></summary>
						<blockquote>
							<details>
								<summary><b>com</b></summary>
								<blockquote>
									<details>
										<summary><b>github</b></summary>
										<blockquote>
											<details>
												<summary><b>nishgpt</b></summary>
												<blockquote>
													<details>
														<summary><b>chainexecutor</b></summary>
														<blockquote>
															<table>
															<tr>
																<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/StageExecutionManager.java'>StageExecutionManager.java</a></b></td>
																<td>- `StageExecutionManager` orchestrates the execution of stages within a defined chain<br>- It manages the lifecycle of each stage, including initialization, execution, and post-completion steps<br>- The manager ensures stages execute sequentially, handles resuming interrupted stages, and facilitates the transition to subsequent stages upon completion or skipping<br>- Error handling and logging are integrated throughout the process.</td>
															</tr>
															</table>
															<details>
																<summary><b>exceptions</b></summary>
																<blockquote>
																	<table>
																	<tr>
																		<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/exceptions/ErrorCode.java'>ErrorCode.java</a></b></td>
																		<td>- ErrorCode defines a set of error codes for the ChainExecutor project<br>- It provides a structured way to represent different types of failures within the chain execution process, improving error handling and reporting<br>- These codes, used throughout the application, facilitate clear identification and management of exceptions related to chain validity, execution stages, and overall completion status.</td>
																	</tr>
																	<tr>
																		<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/exceptions/ChainExecutorException.java'>ChainExecutorException.java</a></b></td>
																		<td>- ChainExecutorException defines custom exceptions for the ChainExecutor project<br>- It provides a structured way to handle errors, including specific error codes and associated messages<br>- The exception class facilitates error propagation and reporting within the application, enhancing debugging and maintainability<br>- Error codes enable more informative error handling throughout the ChainExecutor's execution flow.</td>
																	</tr>
																	</table>
																</blockquote>
															</details>
															<details>
																<summary><b>models</b></summary>
																<blockquote>
																	<details>
																		<summary><b>execution</b></summary>
																		<blockquote>
																			<table>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/execution/ExecutorAuxiliaryKey.java'>ExecutorAuxiliaryKey.java</a></b></td>
																				<td>- ExecutorAuxiliaryKey defines a custom identifier for stage executors within the ChainExecutor project<br>- It serves as a key for registering and retrieving stage executors, enabling efficient management and lookup of execution components<br>- The interface's Serializable nature ensures its suitability for persistence and data transfer.</td>
																			</tr>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/execution/StageExecutor.java'>StageExecutor.java</a></b></td>
																				<td>- `StageExecutor` defines a standard interface for executing processing stages within a larger workflow<br>- It manages stage initialization, execution (including background execution and resumption), post-execution tasks, status validation, and conditional skipping<br>- The interface ensures consistent handling of various stage types and execution contexts across the application.</td>
																			</tr>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/execution/StageExecutorKey.java'>StageExecutorKey.java</a></b></td>
																				<td>- `StageExecutorKey` defines a unique identifier within the ChainExecutor project<br>- It combines a `Stage` object representing a processing step and an `ExecutorAuxiliaryKey` providing additional context, crucial for managing and tracking individual execution units within the larger chain execution process<br>- This ensures unambiguous identification of each stage's execution instance.</td>
																			</tr>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/execution/ExecutionContext.java'>ExecutionContext.java</a></b></td>
																				<td>- ExecutionContext defines a data structure within the ChainExecutor project<br>- It serves as a container for essential execution metadata, primarily an ID, facilitating tracking and management of individual execution instances within the larger application's workflow<br>- This model likely plays a crucial role in organizing and identifying different execution processes.</td>
																			</tr>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/execution/StageExecutorFactory.java'>StageExecutorFactory.java</a></b></td>
																				<td>- StageExecutorFactory provides a mechanism for retrieving stage executors within the ChainExecutor application<br>- It uses reflection and dependency injection to locate and instantiate executors annotated with a specific annotation, mapping them to unique keys for easy retrieval<br>- This facilitates flexible and extensible execution stage management within the overall application architecture.</td>
																			</tr>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/execution/StageExecutionRequest.java'>StageExecutionRequest.java</a></b></td>
																				<td>- StageExecutionRequest defines a contract for objects providing stage execution information within the ChainExecutor project<br>- It ensures consistent access to the Stage object needed for processing, acting as a standardized interface across different execution components<br>- This promotes modularity and facilitates flexible integration of various stage execution strategies.</td>
																			</tr>
																			</table>
																		</blockquote>
																	</details>
																	<details>
																		<summary><b>stage</b></summary>
																		<blockquote>
																			<table>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/stage/StageChain.java'>StageChain.java</a></b></td>
																				<td>- `StageChain` defines the structure for chaining stages within a larger execution process<br>- It represents a directed graph of stages, where each stage's successor is explicitly defined<br>- This model facilitates sequential or conditional execution of processing steps, enabling complex workflows within the `chainexecutor` application<br>- The `head` represents the starting point, and `forwardChainMappings` dictates the flow between subsequent stages.</td>
																			</tr>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/stage/Stage.java'>Stage.java</a></b></td>
																				<td>- `Stage.java` defines a core interface within the `chainexecutor` project's model layer<br>- It represents a single processing stage, providing a standardized `name()` method for identification<br>- This interface likely facilitates the management and execution of sequential processing steps within a larger workflow orchestrated by the `chainexecutor` application<br>- The interface promotes modularity and extensibility in defining individual stages.</td>
																			</tr>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/stage/StageChainRegistry.java'>StageChainRegistry.java</a></b></td>
																				<td>- StageChainRegistry manages and validates execution chains<br>- It stores chains, identified by `StageChainIdentifier`,  allowing retrieval of the next stage given a chain identifier and current stage<br>- Crucially, it ensures chain integrity by detecting loops and broken links, throwing exceptions for invalid configurations<br>- This component is central to the ChainExecutor's workflow, guaranteeing orderly and error-free stage execution.</td>
																			</tr>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/stage/StageChainIdentifier.java'>StageChainIdentifier.java</a></b></td>
																				<td>- `StageChainIdentifier` defines an interface within the `chainexecutor` project's stage model<br>- It serves as a marker interface, likely used for identifying and managing chains of execution stages<br>- This contributes to the overall architecture by providing a mechanism for differentiating and potentially tracking various stage chains within the application's workflow.</td>
																			</tr>
																			<tr>
																				<td><b><a href='https://github.com/nishgpt/chain-executor/blob/master/src/main/java/com/github/nishgpt/chainexecutor/models/stage/StageStatus.java'>StageStatus.java</a></b></td>
																				<td>- StageStatus defines the possible states of processing stages within a chain execution framework<br>- It provides a mechanism for tracking stage progression, identifying terminal states (completed or failed), and determining executability<br>- This enum facilitates robust error handling and workflow management within the larger chain executor application.</td>
																			</tr>
																			</table>
																		</blockquote>
																	</details>
																</blockquote>
															</details>
														</blockquote>
													</details>
												</blockquote>
											</details>
										</blockquote>
									</details>
								</blockquote>
							</details>
						</blockquote>
					</details>
				</blockquote>
			</details>
		</blockquote>
	</details>
</details>

---
##  Getting Started

###  Prerequisites

Before getting started with chain-executor, ensure your runtime environment meets the following requirements:

- **Programming Language:** Java


###  Installation

Install chain-executor using one of the following methods:

**Maven Dependency:**

Use the following maven dependency

```xml

<dependency>
  <groupId>com.github.nishgpt</groupId>
  <artifactId>chain-executor</artifactId>
  <version>0.0.4</version>
</dependency>
```

**Build from source:**

- Clone the source:

      git clone github.com/nishgpt/chain-executor

- Build

      mvn install

---

###  Usage
Import it as a dependency and extend the base classes to construct your chain. The understand how classes work, please read the descriptions above.

---

##  Contributing

- **💬 [Join the Discussions](https://github.com/nishgpt/chain-executor/discussions)**: Share your insights, provide feedback, or ask questions.
- **🐛 [Report Issues](https://github.com/nishgpt/chain-executor/issues)**: Submit bugs found or log feature requests for the `chain-executor` project.
- **💡 [Submit Pull Requests](https://github.com/nishgpt/chain-executor/blob/main/CONTRIBUTING.md)**: Review open PRs, and submit your own PRs.

<details closed>
<summary>Contributing Guidelines</summary>

1. **Fork the Repository**: Start by forking the project repository to your github account.
2. **Clone Locally**: Clone the forked repository to your local machine using a git client.
   ```sh
   git clone https://github.com/nishgpt/chain-executor
   ```
3. **Create a New Branch**: Always work on a new branch, giving it a descriptive name.
   ```sh
   git checkout -b new-feature-x
   ```
4. **Make Your Changes**: Develop and test your changes locally.
5. **Commit Your Changes**: Commit with a clear message describing your updates.
   ```sh
   git commit -m 'Implemented new feature x.'
   ```
6. **Push to github**: Push the changes to your forked repository.
   ```sh
   git push origin new-feature-x
   ```
7. **Submit a Pull Request**: Create a PR against the original project repository. Clearly describe the changes and their motivations.
8. **Review**: Once your PR is reviewed and approved, it will be merged into the main branch. Congratulations on your contribution!
</details>

<details closed>
<summary>Contributor Graph</summary>
<br>
<p align="left">
   <a href="https://github.com{/nishgpt/chain-executor/}graphs/contributors">
      <img src="https://contrib.rocks/image?repo=nishgpt/chain-executor">
   </a>
</p>
</details>

---

##  License

Copyright [2023] Nishant Gupta nishant141077@gmail.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
