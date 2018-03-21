# Software Reversing Lab 2 - Concolic and Learning

First read the following papers, and familiarize yourself with the

KLEE (https://klee.github.io/) and
LearnLib (https://learnlib.de/) tools.

* KLEE: Unassisted and Automatic Generation of High-Coverage Tests for Complex Systems Programs. Cristian Cadar, Daniel Dunbar, and Dawson Engler

* SAGE: whitebox fuzzing for security testing. Godefroid, Patrice, Michael Y. Levin, and David Molnar.

* The open-source LearnLib. Malte Isberner, Falk Howar, and Bernhard Steffen.

* Learning and Testing the Bounded Retransmission Protocol. Fides Aarts, Harco Kuppens, Jan Tretmans, Frits Vaandrager, and Sicco Verwer.

* Model learning. Frits Vaandrager.

You are asked to apply KLEE and LearnLib to the RERS reachability problems (http://rers-challenge.org/). You are free to pick problems from any year, but only the sequential problems, you can choose between LTL and Reachability. Experiment with the tool’s settings and discover which settings (especially LearnLib, try TTT) works best. Report on statistics such as the number of discovered states and performed queries, see the retransmission protocol paper for examples.

Write a report of at most 4 pages covering the following questions:

* What are the sizes of models you can learn using LearnLib?
* Does LearnLib or KLEE reach more statements? Explain why.
* What is the benefit of using KLEE? Explain using your experimental results.
* What is the benefit of using LearnLib? Explain using your results.

Bonus:

The strength of many of the techniques taught in this course increases when they are combined. KLEE and LearnLib are no exception. Study the following paper:

* Complementing Model Learning with Mutation-Based Fuzzing. Rick Smetsers, Joshua Moerman, Mark Janssen, and Sicco Verwer.

This shows how to combine AFL with LearnLib, e.g., by answering equivalence queries using the traces generated by AFL. The bonus task is to combine KLEE and LearnLib in a similar way. Report on how you accomplished this combination and the size increase in learned models. You may add 1 additional page to your report if you do this bonus task.