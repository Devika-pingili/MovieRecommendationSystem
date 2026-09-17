#!/bin/bash

set -e

echo "========================================"
echo " Movie Recommendation System"
echo "========================================"

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$PROJECT_DIR"

JAR="$PROJECT_DIR/MovieRecommendationSystem.jar"

INPUT="/user/pingili_devika/movielens/ratings.csv"

JOB1_OUTPUT="/user/pingili_devika/movielens/output_analysis"

JOB2_OUTPUT="/user/pingili_devika/movielens/output_recommendations"

echo ""
echo "Checking Hadoop..."

if ! command -v hadoop >/dev/null 2>&1; then
    echo "ERROR: Hadoop is not available."
    echo "Make sure you are running this inside WSL."
    exit 1
fi

echo "Hadoop found."

echo ""
echo "Checking input file..."

if ! hdfs dfs -test -e "$INPUT"; then
    echo "ERROR: ratings.csv not found in HDFS."
    echo ""
    echo "Expected location:"
    echo "$INPUT"
    exit 1
fi

echo "Input file found."

echo ""
echo "Removing old Job 1 output..."

hdfs dfs -rm -r -f "$JOB1_OUTPUT"

echo ""
echo "Removing old Job 2 output..."

hdfs dfs -rm -r -f "$JOB2_OUTPUT"

echo ""
echo "========================================"
echo " JOB 1: Movie Rating Analysis"
echo "========================================"

hadoop jar "$JAR" \
    MovieRatingAnalysis \
    "$INPUT" \
    "$JOB1_OUTPUT"

echo ""
echo "Job 1 completed successfully."

echo ""
echo "First 10 records from Job 1:"

hdfs dfs -cat \
    "$JOB1_OUTPUT/part-r-00000" \
    | head -10

echo ""
echo "========================================"
echo " JOB 2: Movie Rating Statistics"
echo "========================================"

hadoop jar "$JAR" \
    MovieRatingStatistics \
    "$JOB1_OUTPUT" \
    "$JOB2_OUTPUT"

echo ""
echo "Job 2 completed successfully."

echo ""
echo "========================================"
echo " RECOMMENDATION RESULTS"
echo "========================================"

hdfs dfs -cat \
    "$JOB2_OUTPUT/part-r-00000" \
    | head -30

echo ""
echo "========================================"
echo " PROJECT COMPLETED"
echo "========================================"