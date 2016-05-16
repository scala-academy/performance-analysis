Periphas
========

Gitter chat: https://gitter.im/scala-academy/performance-analysis
[![Join the chat at https://gitter.im/scala-academy/performance-analysis](https://badges.gitter.im/scala-academy/performance-analysis.svg)](https://gitter.im/scala-academy/performance-analysis?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Waffle issue board: https://waffle.io/scala-academy/performance-analysis
[![Stories in Ready](https://badge.waffle.io/scala-academy/performance-analysis.png?label=ready&title=Ready)](http://waffle.io/scala-academy/performance-analysis)

Build status:
[![Build Status](https://travis-ci.org/scala-academy/performance-analysis.svg?branch=develop)](https://travis-ci.org/scala-academy/performance-analysis)

Codacy grade and coverage:
[![Codacy Badge](https://api.codacy.com/project/badge/grade/99aa6d53ff6644899303a0ce71b733a2)](https://www.codacy.com/app/scala-academy/performance-analysis)
[![Codacy Badge](https://api.codacy.com/project/badge/coverage/99aa6d53ff6644899303a0ce71b733a2)](https://www.codacy.com/app/scala-academy/performance-analysis)

This is a project used in the "Scala and akka in practise" course.
Goal: learn programing with Scala and Akka in a real-world scenario.

Periphas is short for PERFormance AnalyticS and was a well loved king in Ancient Greece, known for his righteousness (source: https://en.wikipedia.org/wiki/Periphas_(Attic_king))

Run Gatling Simulations
=======================

To run all gatling tests

`sbt gatling:test`

To run single test

`sbt gatling:testOnly <package.simulation.class>`