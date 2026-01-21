import csv

print('QUESTION 1 - String Slicing')
print()

s = 'HumptyDumptysatonawallHumptyDumptyhadagreatfallAlltheKingshorsesandalltheKingsmenCouldntputHumptyDumptyinhisplaceagain.'

word1 = s[22:28] # indexes specific letter in long string to certain index exclusive
word2 = s[97:103] # indexes specific letter to exclusive, making sure to add 1 to end to cache last letter
print(word1 + " " + word2)
# printing space between words, not auto inserted

print()
print('QUESTION 2 - Dictionary of Tuples')
print()

#with open('CryptoImport.csv', mode='r', encoding = "utf-8-sig") as inp: # imports csv file in working directory of script
with open('CryptoImport.csv', mode='r', encoding = "utf-8-sig") as inp: # imports csv file in working directory of script
    # uses encoding key to redact memory location at beginning of dict
    reader = csv.reader(inp)
    dict_from_csv = {rows[0]:(float(rows[1]),float(rows[2])) for rows in reader} # formats dictionary with for loop iterating thru reader var of csv

print(dict_from_csv)

print()
print('QUESTION 2a - Amount Invested')
print()

# finding total investment based on amt owned ** worth of indiv coin
total_investment = sum(amount * price for amount, price in dict_from_csv.values())
# multiplies amount and price (which are iterable variables created in loop) by each other, iterating through float_tuples of dict

print(f"Total Investment: ${total_investment:.2f}") # .2f helps format the investment value by rounding to two decimal places for money (stackoverflow)

print()
print('QUESTION 3 - Dogecoin Price Increase')
print()

new_doge_price = 747.15
# updating price for dogecoin in tuple dictionary
dict_from_csv['DOGE'] = (dict_from_csv['DOGE'][0], new_doge_price)
#print(dict_from_csv)

print()
print('QUESTION 4 - Printing Dogecoin Value of Investment')
print()

doge_owned, doge_price = dict_from_csv['DOGE'] # caches values in dict based on DOGE key - caches as amt owned and price/value of currency
doge_investment_value = doge_owned * doge_price # multiply two vlaues like in prob 2a

print(f"My Dogecoin value is now: ${doge_investment_value:.2f}") # .2f round formatting being used again