#1 Store items and price in dict
shop_dict = {"mittens": 30, "socks": 40, "scarves": 65, "hats": 35, "potholders": 10}
# no output required, just caching of values in dict

#2
print('QUESTION 2')
print()
# Print keys (items)
print(", ".join(item for item in shop_dict.keys()))
# (W3Schools) - .join() takes everything being outputted from loop and concatenates into one solid string (W3Schools)
# Print the values (prices)
# uses print-f for formatting values of dict integrated with for loop
print(", ".join(f"${price}" for price in shop_dict.values()))
# .join() takes everything being outputted from loop and concatenates into one string
# print would have otherwise printed on separate lines
print()


#3
print('QUESTION 3')
print()
expensive_item = max(shop_dict, key=shop_dict.get)
# Uses max function to identify key with associated "max" value
expensive_price = shop_dict[expensive_item]
# indexing identifies actual value of dict indexed based off of "max" key

# Output result in formatted string
print(f"The most expensive item is {expensive_item} at ${expensive_price}.")
print()

#4
print('QUESTION 4')
print()

# Items mom bought
purchased_items = ["scarves", "potholders"]

# initializing total sale amount to calculate total receipt
total_cost = 0

# printing each item and its price, and calculating total purchase
for item in purchased_items:
    price = shop_dict[item]
    print(f"{item}: ${price}")
    total_cost += price
# iterating through array of products that mom bought, values in bought_products array should match corresponding keys in dict
# connect matching price/value with product in dict, print out, and add value in dict to separate variable for final purchase price
# outputting total purchase
print(f"Total Sale: ${total_cost}")
