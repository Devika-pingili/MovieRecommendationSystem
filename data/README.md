# Dataset

This project uses the **MovieLens 32M dataset**.

## Download Dataset

The dataset can be downloaded from Kaggle:

[Download MovieLens 32M Dataset from Kaggle](https://www.kaggle.com/datasets/justsahil/movielens-32m?utm_source=chatgpt.com)

Download the dataset and extract the files.

The dataset contains:

```text
ml-32m/
├── links.csv
├── movies.csv
├── ratings.csv
└── tags.csv
```

### Dataset Files

| File          | Columns                                    | Purpose                       |
| ------------- | ------------------------------------------ | ----------------------------- |
| `ratings.csv` | `userId`, `movieId`, `rating`, `timestamp` | Main input for MapReduce      |
| `movies.csv`  | `movieId`, `title`, `genres`               | Movie information             |
| `tags.csv`    | `userId`, `movieId`, `tag`, `timestamp`    | User-generated tags           |
| `links.csv`   | `movieId`, `imdbId`, `tmdbId`              | External movie database links |

---

## `ratings.csv`

`ratings.csv` is the main input file for this project.

It is approximately **800+ MB**, so it is **not included in this GitHub repository**.

After downloading and extracting the dataset, copy `ratings.csv` into the project's `data/` folder:

```text
data/
├── links.csv
├── movies.csv
├── ratings.csv
└── tags.csv
```

The smaller files `links.csv`, `movies.csv`, and `tags.csv` are already included in this repository.

---

# Columns Used in Our Project

Our two MapReduce jobs use only **two columns from `ratings.csv`**:

| Column      | Used | Purpose                       |
| ----------- | ---- | ----------------------------- |
| `movieId`   | Yes  | Identifies the movie          |
| `rating`    | Yes  | Rating given by the user      |
| `userId`    | No   | Not required for the analysis |
| `timestamp` | No   | Not required for the analysis |

Therefore, the actual data used by the MapReduce processing is:

```text
movieId
rating
```

---

# Job 1 – MovieRatingAnalysis

### Input

```text
ratings.csv
```

The mapper reads the required columns:

```text
movieId
rating
```

### Mapper

For every rating record, the mapper produces:

```text
movieId → rating
```

Example:

```text
1 → 4.0
1 → 5.0
1 → 3.0
2 → 4.5
```

The same `movieId` will therefore have multiple rating values.

### Reducer

The reducer receives all ratings belonging to the same `movieId`.

It calculates:

* **Count** = number of ratings
* **Sum** = sum of all ratings

### Job 1 Output

```text
movieId    count    sum
```

Example:

```text
1    68997    269088.3
2    29449    94900.2
```

So the transformation is:

```text
movieId + rating
        ↓
   Job 1 Mapper
        ↓
movieId → rating
        ↓
   Job 1 Reducer
        ↓
movieId + count + sum
```

---

# Job 2 – MovieRatingStatistics

Job 2 takes the output of Job 1 as its input.

### Input

```text
movieId    count    sum
```

### Mapper

The mapper reads:

```text
movieId
count
sum
```

It calculates the average rating:

```text
Average Rating = Sum / Count
```

It then filters the movies using:

```text
Count >= 100
Average Rating >= 4.0
```

Only movies satisfying both conditions are passed to the reducer.

### Reducer

The reducer writes the filtered records to the final output.

### Job 2 Output

```text
movieId    count    averageRating
```

Example:

```text
1    68997    3.90
10   32474    3.43
```

So the transformation is:

```text
movieId + count + sum
        ↓
   Job 2 Mapper
        ↓
calculate average
        ↓
apply filters
        ↓
   Job 2 Reducer
        ↓
movieId + count + averageRating
```

---

# Complete Data Flow

```text
                 ratings.csv
                     │
                     │
             movieId + rating
                     │
                     ▼
        ┌────────────────────────┐
        │  MovieRatingAnalysis   │
        │        Job 1           │
        └────────────────────────┘
                     │
                     │
             movieId + count + sum
                     │
                     ▼
        ┌────────────────────────┐
        │ MovieRatingStatistics  │
        │        Job 2           │
        └────────────────────────┘
                     │
                     │
       movieId + count + averageRating
                     │
                     ▼
                Final Output
```

## Summary

| Job           | Input Columns             | Processing                             | Output Columns                      |
| ------------- | ------------------------- | -------------------------------------- | ----------------------------------- |
| Job 1 Mapper  | `movieId`, `rating`       | Groups ratings by movie                | `movieId → rating`                  |
| Job 1 Reducer | `movieId`, `rating`       | Calculates count and sum               | `movieId`, `count`, `sum`           |
| Job 2 Mapper  | `movieId`, `count`, `sum` | Calculates average and applies filters | `movieId`, `count`, `averageRating` |
| Job 2 Reducer | Filtered records          | Writes final result                    | `movieId`, `count`, `averageRating` |

### Important Note

The current MapReduce implementation does **not** join `movies.csv` with the rating data. Therefore, the final output contains **`movieId`**, not movie titles.

