#!/bin/bash

set -e

echo "========================================"
echo " Building Movie Recommendation System"
echo "========================================"

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$PROJECT_DIR"

echo ""
echo "Creating build directory..."

rm -rf build
mkdir -p build/classes

echo ""
echo "Compiling Java files..."

javac \
    -classpath "$(hadoop classpath)" \
    -d build/classes \
    src/MovieRatingAnalysis.java \
    src/MovieRatingStatistics.java

echo ""
echo "Creating JAR file..."

jar -cvf MovieRecommendationSystem.jar \
    -C build/classes .

echo ""
echo "========================================"
echo " Build completed successfully!"
echo "========================================"

echo ""
echo "Generated JAR:"
echo "$PROJECT_DIR/MovieRecommendationSystem.jar"