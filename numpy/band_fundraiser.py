import numpy as np
import csv

students = {}
with open('names _1_.csv', mode='r') as csvfile:
    reader = csv.DictReader(csvfile)
    
    for row in reader:
        # Use the value in the first column as the key and the value in the second column as the value
        students[row[reader.fieldnames[0]]] = row[reader.fieldnames[1]]

sales = {}
with open('sales _1_.csv', mode='r') as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        student = row['\ufeffstudent']
        sale = row['sale'].strip()

        # Append sale to the student's entry, initializing a new list if necessary
        if student in sales:
            sales[student].append(sale)
        else:
            sales[student] = [sale]

# 1 - how many bags of popcorn did the band sell?
num_of_popcorn_bags = 0




