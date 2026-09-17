# Movie Recommendation System using Hadoop MapReduce

## Overview

This project implements a Movie Recommendation System using Apache Hadoop MapReduce.

The system processes the MovieLens 32M ratings dataset and identifies movies that have high average ratings and a sufficient number of user ratings.

The project contains exactly two MapReduce jobs:

1. `MovieRatingAnalysis`
2. `MovieRatingStatistics`

The project is designed to run on a single computer using Hadoop inside WSL.

---

## Project Structure

```text
MovieRecommendationSystem/
│
├── src/
│   ├── MovieRatingAnalysis.java
│   └── MovieRatingStatistics.java
│
├── data/
│   └── README.md
│
├── output/
│   └── README.md
│
├── conf/
│   └── README.md
│
├── scripts/
│   ├── build.sh
│   └── run.sh
│
├── README.md
└── .gitignore
```

---

# Technologies Used

* Java
* Apache Hadoop
* Hadoop HDFS
* Hadoop MapReduce
* YARN
* WSL Ubuntu
* Visual Studio Code

---

# Dataset

The project uses the MovieLens 32M dataset.

The main file required by the project is:

```text
ratings.csv
```

The file contains four columns:

```text
userId
movieId
rating
timestamp
```

Example:

```text
1,296,5.0,1147880044
1,306,3.5,1147868817
2,296,4.0,1147869191
```

The complete dataset is not stored in this repository because of its large file size.

---

# System Workflow

```text
                   ratings.csv
                       |
                       v
        +-----------------------------+
        |     MovieRatingAnalysis     |
        |            JOB 1            |
        +-----------------------------+
                       |
                       v
             movieId -> total,count
                       |
                       v
        +-----------------------------+
        |    MovieRatingStatistics    |
        |            JOB 2            |
        +-----------------------------+
                       |
                       v
                 Average Rating
                       |
                       v
              Recommendation Filter
                       |
                       v
              Recommended Movies
```

---

# Job 1: MovieRatingAnalysis

## Purpose

The first MapReduce job groups ratings according to `movieId`.

For every movie, it calculates:

* Total rating
* Number of ratings

## Mapper

The Mapper reads records from `ratings.csv`.

Input:

```text
userId,movieId,rating,timestamp
```

The Mapper emits:

```text
movieId -> rating,1
```

Example:

```text
296 -> 5.0,1
296 -> 4.0,1
296 -> 5.0,1
```

## Reducer

The Reducer combines all ratings belonging to the same movie.

Example:

```text
296 -> 14.0,3
```

This means:

```text
Movie ID       = 296
Total Rating   = 14.0
Rating Count   = 3
```

---

# Job 2: MovieRatingStatistics

## Purpose

The second MapReduce job calculates the average rating for every movie.

Formula:

```text
Average Rating = Total Rating / Number of Ratings
```

The job then filters movies based on the recommendation criteria.

## Recommendation Criteria

A movie is considered recommended when:

```text
Average Rating >= 4.0
```

and:

```text
Number of Ratings >= 100
```

The rating-count condition prevents movies with only a small number of ratings from being considered strong recommendations.

---

# Final Output

The final output has the following format:

```text
movieId    averageRating    ratingCount
```

Example:

```text
318    4.43    982
296    4.20    1460
858    4.32    875
```

These movies satisfy the recommendation criteria.

---

# Hadoop Setup

The project is designed to run with Hadoop installed inside WSL Ubuntu.

Before executing the MapReduce jobs, make sure HDFS and YARN are running.

Start HDFS:

```bash
start-dfs.sh
```

Start YARN:

```bash
start-yarn.sh
```

Check Hadoop processes:

```bash
jps
```

Typical processes are:

```text
NameNode
DataNode
SecondaryNameNode
ResourceManager
NodeManager
```

Check HDFS:

```bash
hdfs dfsadmin -report
```

---

# Upload Dataset to HDFS

Create the HDFS directory:

```bash
hdfs dfs -mkdir -p /user/pingili_devika/movielens
```

Upload `ratings.csv`:

```bash
hdfs dfs -put ratings.csv /user/pingili_devika/movielens/
```

Check:

```bash
hdfs dfs -ls /user/pingili_devika/movielens
```

The following file should be present:

```text
ratings.csv
```

If the file has already been uploaded, this step does not need to be repeated.

---

# Build the Project

Move into the project directory:

```bash
cd ~/hadoop-project/MovieRecommendationSystem
```

Make the build script executable:

```bash
chmod +x scripts/build.sh
```

Run:

```bash
./scripts/build.sh
```

The script:

1. Creates the build directory.
2. Compiles both Java files.
3. Creates the Hadoop JAR file.

Generated file:

```text
MovieRecommendationSystem.jar
```

---

# Run the Complete Project

Make the run script executable:

```bash
chmod +x scripts/run.sh
```

Run:

```bash
./scripts/run.sh
```

The script automatically executes:

```text
Job 1
  ↓
output_analysis
  ↓
Job 2
  ↓
output_recommendations
```

It also removes old Hadoop output directories before running the jobs again.

---

# Run Job 1 Manually

Remove old Job 1 output:

```bash
hdfs dfs -rm -r -f /user/pingili_devika/movielens/output_analysis
```

Run Job 1:

```bash
hadoop jar MovieRecommendationSystem.jar \
MovieRatingAnalysis \
/user/pingili_devika/movielens/ratings.csv \
/user/pingili_devika/movielens/output_analysis
```

Check Job 1 output:

```bash
hdfs dfs -cat \
/user/pingili_devika/movielens/output_analysis/part-r-00000
```

Display only the first 20 records:

```bash
hdfs dfs -cat \
/user/pingili_devika/movielens/output_analysis/part-r-00000 | head -20
```

---

# Run Job 2 Manually

Remove old Job 2 output:

```bash
hdfs dfs -rm -r -f /user/pingili_devika/movielens/output_recommendations
```

Run Job 2:

```bash
hadoop jar MovieRecommendationSystem.jar \
MovieRatingStatistics \
/user/pingili_devika/movielens/output_analysis \
/user/pingili_devika/movielens/output_recommendations
```

View final recommendations:

```bash
hdfs dfs -cat \
/user/pingili_devika/movielens/output_recommendations/part-r-00000
```

Display the first 30 recommendations:

```bash
hdfs dfs -cat \
/user/pingili_devika/movielens/output_recommendations/part-r-00000 | head -30
```

---

# VS Code Execution

The Java files can be developed using Visual Studio Code.

The recommended setup is:

```text
Windows
   |
   v
Visual Studio Code
   |
   v
WSL Ubuntu
   |
   v
Apache Hadoop
   |
   +---- HDFS
   |
   +---- YARN
   |
   +---- MapReduce
```

Open WSL:

```bash
wsl
```

Go to the project:

```bash
cd ~/hadoop-project/MovieRecommendationSystem
```

Open the project in VS Code:

```bash
code .
```

In VS Code, open:

```text
Terminal -> New Terminal
```

Make sure the terminal is a WSL/Ubuntu terminal.

Check Java:

```bash
java -version
```

Check Hadoop:

```bash
hadoop version
```

Then build:

```bash
./scripts/build.sh
```

Run:

```bash
./scripts/run.sh
```

---

# WSL vs VS Code

There is no separate Java code for VS Code and WSL.

VS Code is used for:

* Writing Java code
* Editing files
* Managing the project
* Opening the terminal

WSL is used as the Linux environment where Hadoop is installed and executed.

Therefore, when VS Code is connected to WSL, the Hadoop commands are the same as the commands used directly in the WSL terminal.

---

# Windows PowerShell

If Hadoop is installed only inside WSL, Hadoop commands should not be executed directly from Windows PowerShell.

Enter WSL first:

```powershell
wsl
```

Then:

```bash
cd ~/hadoop-project/MovieRecommendationSystem
```

Build:

```bash
./scripts/build.sh
```

Run:

```bash
./scripts/run.sh
```

---

# Important HDFS Output Rule

Hadoop requires the output directory to be new.

For example, if this directory already exists:

```text
/user/pingili_devika/movielens/output_analysis
```

Job 1 will fail.

Remove it before running Job 1 again:

```bash
hdfs dfs -rm -r -f /user/pingili_devika/movielens/output_analysis
```

Similarly, remove Job 2 output before rerunning:

```bash
hdfs dfs -rm -r -f /user/pingili_devika/movielens/output_recommendations
```

The `run.sh` script performs these operations automatically.

---

# Useful Commands

Check HDFS root:

```bash
hdfs dfs -ls /
```

Check project directory:

```bash
hdfs dfs -ls /user/pingili_devika/movielens
```

Check Job 1 output:

```bash
hdfs dfs -ls /user/pingili_devika/movielens/output_analysis
```

Check Job 2 output:

```bash
hdfs dfs -ls /user/pingili_devika/movielens/output_recommendations
```

Read final results:

```bash
hdfs dfs -cat /user/pingili_devika/movielens/output_recommendations/part-r-00000
```

---

# GitHub Notes

The complete MovieLens dataset should not be uploaded to GitHub because of its large size.

Generated Hadoop output should also not be uploaded.

The repository contains:

* Java source code
* Build script
* Run script
* README files
* Project documentation

The dataset remains in HDFS.

---

# Project Features

This project demonstrates:

* Apache Hadoop
* HDFS
* YARN
* MapReduce
* Mapper
* Reducer
* Shuffle and Sort
* Large-scale data processing
* Movie rating aggregation
* Average rating calculation
* Recommendation filtering

---

# Conclusion

The Movie Recommendation System uses two Hadoop MapReduce jobs to process movie ratings.

`MovieRatingAnalysis` aggregates ratings for each movie, while `MovieRatingStatistics` calculates average ratings and filters movies based on rating quality and rating count.

The project runs on a single computer using Hadoop inside WSL and can be developed and executed through Visual Studio Code.
