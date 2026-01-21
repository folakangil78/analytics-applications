from csv import DictReader
import csv # need to import for csv.DictReader to work

print('QUESTION 1: Read in file')
print()
with open('ParsingPractice.csv', mode='r', newline='', encoding='utf-8') as input_file: # imports csv file from working direc of script
    csv_reader = csv.DictReader(input_file) # keys are column header labels when using DictReader

    # iterating each row in csv
    for row in csv_reader:
        print(row)
    print()

print('QUESTION 2: List of Dictionaries')
print()

# initialize empty list to store dictionaries
list_of_gene_dicts = []

with open('ParsingPractice.csv', mode='r', newline='', encoding='utf-8') as input_file: 
    csv_reader = csv.DictReader(input_file) # re-insert with-open block for file from question 1 because csv_reader is local to with-block

    # iterating over rows as separate dictionaries and append each row-dictionary to list
    for row in csv_reader:
        list_of_gene_dicts.append(dict(row))
    print(list_of_gene_dicts)
    print()

print('QUESTION 3: # of Genomes per VF')
print()

output_file = 'ProcessedGenomes.csv'
# function to sum all the quantities in a row (after the first three columns)
def calculate_genomes(row, headers):
    total_genomes = 0
    for header in headers[3:]:  # skips first three columns in csv, starts after
        values = row[header].split(';')  # differentiation by semicolon
        # Convert each value to an integer and sum it up
        total_genomes += sum(int(value) for value in values if value.isdigit()) #this line is breaking for some reason but is trying to convert element in csv slot to a number to add
    return total_genomes

# read in csv and process
with open('ParsingPractice.csv', mode='r', newline='', encoding='utf-8') as input_file:
    csv_reader = csv.DictReader(input_file)
    headers = csv_reader.fieldnames  # cache header names (even though only first three are useful)
    
    # creating output file
    with open(output_file, mode='w', newline='', encoding='utf-8') as output_file:
        # Define the fieldnames for the output CSV
        fieldnames = headers[:3] + ['Total Genomes']
        csv_writer = csv.DictWriter(output_file, fieldnames=fieldnames)
        
        # append header to new csv
        csv_writer.writeheader()
        
        # process each row
        for row in csv_reader:
            total_genomes = calculate_genomes(row, headers)
            # create new row with the first three columns and  total sum
            output_row = {
                headers[0]: row[headers[0]],  # VF Class
                headers[1]: row[headers[1]],  # Virulence Factor
                headers[2]: row[headers[2]],  # Related Genes
                'Total Genomes': total_genomes  # New column for total gens
            }
            # write row to the output CSV - summation not working!!!
            csv_writer.writerow(output_row)

print(f"Summing of genome variations complete. File outputted to {output_file}")





