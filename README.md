# Evolutionary Games on the Lattice

This is an archive of my Honors Thesis from my Bachelor's program in Computational Mathematical Sciences from Arizona State University in 2012.

It reflects ... where I came from.

In a miniscule way, this simulation and minimal thesis reinforce Peter Kropotkin's theory of Mutual Aid as a Factor of Evolution, though I didn't know anything about that at the time of writing.

## Build

### Java Simulator

I seem to have left this project in an unusable state that was headed towards a browser applet, so I hacked together a main class that seems to work how I remember. To build & run, from the `src/` directory, execute `javac *.java && java Simulator`.

### LaTeX

If you have LaTeX tooling installed, you can run `pdflatex` or equivalent in the `TeX/` directory to build the Thesis PDF (`Thesis.tex`) and the associated slideshow (`ThesisPresentation.tex`). There's also something called `Verbal Description.tex`, and your guess is as good as mine.

## Using the Simulator

This project is a testament to my user interface design skills. I have gotten no better at this in the intervening years.

The payoff matrix grid on the simulator matches the matrices as written in the paper:

a11 | a12
a21 | a22

I'll type the grids out as `[[a11, a21], [a21, a22]]`, in honor of mathematicians' maddening habit of insisting vectors be represented vertically, flying in the face of reasonable coordinate systems.

This means that the payoff when both players choose strategy 1 is a11; the payoff when both players choose strategy 2 is a22; the payoff A receives for choosing strategy 1 while B chooses 2 is a12; and the payoff A receives for choosing strategy 2 while B chooses strategy 1 is a21.

In the visual simulation, strategy 1 is represented as black and strategy 2 is white.

Most of the interesting results are in 2 dimensions with 2 competing strategies. You can simulate the payoff matrices in the thesis by entering the corresponding payoff coefficients in the 2x2 grid below. The bifurcation diagrams in the paper have a21 and a12 set to 6 and 3 respectively, which means you'll want the bottom-left set to 6 and the top-right set to 3. a11 is the top-left, a22 the bottom-right.

You can also select from the 8 "update methods" described in the thesis. If you learn anything about what they represent or how they work, you're beyond me as I exist in 2020.

To begin with, just click Start with the default `[[6, 6], [3, 3]]` matrix. You'll see a stable equilibrium in terms of the relative populations of black and white squares. Change the top-left value to `1.001` and watch as white slowly takes over the field. From there you can get an idea of how the coefficients affect the outcomes.

To simulate the Prisoner's Dillemma result of which we are all oh-so-proud, start with a payoff matrix of `[[5, 6], [3, 3.01]]` and choose the Imitation Process (it's fourth but corresponds to Group 3). With this setup, Strategy 1, black, is Cooperation and Strategy 2, white, is Defection. With the bifurcation diagrams on page 21, you can vary the coefficients from there and see if the result holds. I think the edges of the bifurcations might actually be off, but who's to say.

Another fun one is to choose the Best-Response Dynamics method and set the grid to `[[6, 6], [3, 3]]`. Total static! Now pause and tick a11 and a22 up by 0.01 each for `[[6.01, 6], [3, 3.01]]`. A whole different thing! Pause and try `[[6, 6.01], [3, 3.01]]`. What's that! It just looks neat. And why can you pause it by ticking a12 to 3.02? Wild! It quickly reaches a perfect chessboard equilibrium, too. Such tiny changes. Strangely, `[[6, 6.001], [3.001, 3]]` freezes almost immediately - you'll notice that the time keeps ticking; it's just that the process can't find "better" strategies for any cells in the grid. I don't really remember the details but the Best Response Dynamics process is really distinct from the others.

The simulation always stops when the ratio of any one strategy reaches 1.0. For reasons that I have no doubt are sensible and/or unavoidable, the rendered simulation stops slightly before reflecting that fact.

Dr. Lanchier and I never really did much with the three-strategy versions, but with the spinners set to vary only by 0.01, I think you can probably mess around and make some fun animations. Enjoy.

## Conclusions

From a cursory review of this paper & project, I see that I used to be even more pretentious than I currently am. Chalk that up to progress.

-Andrea
