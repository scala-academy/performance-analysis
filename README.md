Performance analysis
====================

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

What do you call a Java repository?
A garbage collection<Java>

Run Gatling Simulations
=======================

To run all gatling tests

`sbt gatling:test`

To run single test

`sbt gatling:testOnly <pacakge.simulation.class>`

Actor Design
============

![Alt text](http://g.gravizo.com/g?
  digraph G {
    node [shape=box];
    Administrator [shape=oval];
    LogReceiver [shape=oval];
    aize ="4,4";
    AdministratorActor;
    LogParserActor;
    LogReceiverActor;
    ;
    Administrator -> AdministratorActor [label="RegisterMetric\\nGetRegisteredComponents\\nRegisterComponent\\nGetDetails"];
    ;
    AdministratorActor -> Administrator [label="MetricCreated\\nRegisteredComponents\\nLogParserCreated\\nLogParserExisted\\nDetails\\nLogParserNotFound"];
    ;
    AdministratorActor -> LogParserActor [label="RequestDetails\\nMetric"];
    LogParserActor -> AdministratorActor [label="Details\\nMetricRegistered"];
    ;
    LogReceiver -> LogReceiverActor [label="SubmitLogs"];
  }
)
