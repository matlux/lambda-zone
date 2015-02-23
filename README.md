# Lambda Zone

A Clojure website for chess strategy functions

[![Build Status](https://travis-ci.org/matlux/lambda-zone?branch=master)](https://travis-ci.org/matlux/lambda-zone)

Go to this [video](https://skillsmatter.com/skillscasts/5336-adatx-test-driven-development-lambda-chess) which explains the concepts of this application.

[lambda.zone](http://lambda.zone) is a website which is similar to TryClojure and 4Clojure websites, it provides developers a platform to submit their own code, in this case a Chess strategy written in Clojure. Their aim is to compete to get the highest ranking.

Developers just need to implement a single function which the platform executes against every other algorithms previously submitted and updates the stats.

The platform publicly lists the ranking of each algorithm and enables human players to measure themselves interactively against a published strategy of their choice.

In this presentation we will review the design choices of the application and analyse how Clojure is providing a unique set of features which are necessary to implement a platform of this kind.

Check out the slides from Mathieu's talk via the following [link!](http://slides-skillsmatter.lambda.zone/#/)

## Usage

Go to the live Website [www.lambda.zone](http://lambda.zone).

Documentation available [under](https://github.com/matlux/lambda-zone/wiki/Chess).

## Structure of the project

[http://lambda-zone](http://lambda-zone) is a website composed of various components available on GitHub:

* [lambda-zone](https://github.com/matlux/lambda-zone) - website source code
* [clj-chess-engine](https://github.com/matlux/clj-chess-engine) - pure chess engine code
* [lambda-chess](https://github.com/matlux/lambda-chess) - chess strategy SDK for website user to use to develop their own strategy.
* [connect4](https://github.com/matlux/clj-chess-engine) - pure connect4 Engine

This forum is to discuss about the lambda.zone platform current and future features.


## Join the [Google group](https://groups.google.com/forum/#!forum/lambda-zone) discussion

[https://groups.google.com/forum/#!forum/lambda-zone](https://groups.google.com/forum/#!forum/lambda-zone)

## License

Copyright © 2014 Matlux

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
