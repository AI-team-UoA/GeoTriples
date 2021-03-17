# Datasets

## You can find the Datasets **[here](https://drive.google.com/file/d/1mVsn7aXozR0uX4KXvsLr-bY8t1eZDpP3/view?usp=sharing)**

In the above link you will find the datasets that have been tested with GeoTriples and GeoTriples-Spark. It contains multiple ESRI Shapefiles
originated from OSM and GADM, and a CSV and a GeoJSON documents. Also, you will find instructions on how to replicate the CSV document in order to produce the big CSV documents that were used in the experiments of GeoTriples-Spark. 

Below you will find some details about the datasets

### Shapefiles
|      Name     |                                  Description                                 | #Geometric features | SHP size (MB) | DBF size (MB) | Total Size (MB) |
|:-------------:|:----------------------------------------------------------------------------:|:-------------------:|:-------------:|:-------------:|:---------------:|
|  Andorra_huge |  A modified SHP  containing the  administrative units  of Andorra, from GADM  |        367388       |      626      |      264      |       889       |
| Australia_big | A modified SHP  containing the  administrative units  of Australia, from GADM |         132         |      247      |      0.1      |       247       |
|    Ukraine    |                  Administrative units  of Ukraine, from GADM                  |          27         |      2.1      |     0.024     |       2.2       |
|    roads_gr   |                        Road system of  Greece, from OSM                       |        857338       |      285      |      148      |       440       |
|    roads_at   |                       Road system of  Austria, from OSM                       |       1876309       |      426      |      324      |       764       |
|    roads_es   |                        Road system of  Spain, from OSM                        |       3658745       |      632      |      1004     |       1663      |
|    roads_de   |                       Road system of  Germany, from OSM                       |       11107532      |      1785     |      1918     |       3787      |


## CSV/GeoJSON

|     Name    | File Type | #Geometric features | Total Size (MB) |
|:-----------:|:---------:|:-------------------:|:---------------:|
| 1GB.geojson |  GeoJSON  |       1550231       |       1036      |
|   1GB.csv   |    CSV    |       5607533       |       1098      |
