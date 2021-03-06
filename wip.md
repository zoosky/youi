# Work in Progress
## Tasks currently being worked on for the next release

* [X] Migration of `ui` module to be a cross-project instead of Scala.js only
* [X] Added RoundedRectangle to drawable functionality
* [X] Create `utilities` module for internal code generation and other external functionality
    * [X] Create `KeyBuilder` to support MDN standards generation of `Key` class from https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values
* [X] Update `Key` to use `KeyboardEvent.key` as mapping for better compliance to latest spec
* [X] Replace direct use of `KeyboardEvent` in hypertext and component with `KeyEvent`
* [ ] Layout support in AbstractContainer
    * [ ] VerticalLayout
* [X] Update dom Template to utilize Profig instead of limiting to environment variable for load path
* [X] New `spatial` package to do matrix and other spatial math
* [ ] Remove uPickle and rely exclusively on Circe for pickling
* [X] Add better functionality to build FormDataContent
* [X] Support multipart form posting in youi-client
* [X] Update URL interpolator to only accept literals for proper compile-time validation