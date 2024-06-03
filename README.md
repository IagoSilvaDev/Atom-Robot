# Atom Robot for Robocode

## Description

The Atom Robot is a competitive robot designed for Robocode, created for a class competition in the Machine Learning course at Uninassau University. Leveraging advanced movement strategies and machine learning techniques, Atom aims to outperform opponents by dynamically adjusting its strategy to maximize hit probability using logistic regression.

## Features

- **Advanced Movement**: Detects and reacts to battlefield boundaries, avoiding collisions and strategically changing direction.
- **Adaptive Firing**: Utilizes both linear and head-on targeting strategies, selecting the optimal one based on previous performance and calculated hit probabilities.
- **Machine Learning**: Employs logistic regression to estimate the probability of hitting an enemy, adjusting firing power accordingly.
- **Data Collection**: Gathers and saves training data during battles, which can be used for further analysis and improvement.

## How to Run

1. **Install Robocode**: Ensure you have Robocode installed on your system.
2. **Download Atom**: Obtain the `Atom.jar` file and the source code for the `Atom` and `LogisticRegression` classes from the repository.
3. **Import to Robocode**:
    - Open Robocode.
    - Navigate to `Robot -> Import Robot or Team`.
    - Select the `Atom.jar` file from the directory where you downloaded it.
4. **Start a Battle**: Open Robocode, select Atom as your robot, and start a battle.
