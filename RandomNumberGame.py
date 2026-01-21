from random import randint

#1 - Continuous Guessing
print('QUESTION 1')
secret_number = randint(1, 10)
print(secret_number)

guess = int(input("What number am I thinking of? Type a guess between 1 and 10 here: "))

while True:
    if guess != secret_number:
        guess = int(input("That's not the number that I'm thinking of. Guess again: "))
    else:
        # uses else statement to print only if number matches
        print("You guessed it!")
        print()
        break

# #2 Validating Print Statement
print('QUESTION 2')
secret_number = randint(1, 10)
print(secret_number)

guess = int(input("What number am I thinking of? Type a guess between 1 and 10 here: "))
while True:
    if guess != secret_number:
        guess = int(input("That's not the number that I'm thinking of. Guess again: "))
    else:
        # uses quantity of variable within print statement to dynamically print whatever that value is
        print("Congratulations on guessing correctly! The secret number was", secret_number)
        print()
        break

#3 Input/Hint-based Guessing Game
print('QUESTION 3')

secret_number = randint(1, 10)
print(secret_number)

guess = int(input("What number am I thinking of? Type a guess between 1 and 10 here: "))

# adds value to a set structure
guess_record = set()

if guess == secret_number:
    print("That's correct!")
elif guess != secret_number:
    while True:
        if guess in guess_record:
            guess = int(input('You already guess that number! Guess again: '))
            # uses different hints based on where value is to help user guess
        elif guess > secret_number:
            guess_record.add(guess)
            guess = int(input('Not quite. That number was too high. Guess again: '))
        elif guess < secret_number:
            guess_record.add(guess)
            guess = int(input('Not quite. That number was too low. Guess again: '))
        else:
            print()
            print("That's right - you guessed it!")
            print()
            # breaks infinite loop
            break
print(f'This is the record of your guesses until you got the right number: {guess_record} ')