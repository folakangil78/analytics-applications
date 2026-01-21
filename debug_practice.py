# class grades are determined as follows:
# each test is worth 25%
# each homework assignment is worth 10% updated to 12.5%
class_grades = {
    'Harry': {'tests': [75, 80], 'homeworks': [80, 75, 80, 85, 90]},
    'Hermione': {'tests': [100, 100], 'homeworks': [100, 100, 100, 100, 100]},
    'Ron': {'tests': [70, 80], 'homeworks': [70, 65, 80, 70, 75]},
    'Malfoy': {'tests': [80, 80], 'homeworks': [75, 80, 70, 80, 75]},
    'Luna': {'tests': [100, 80], 'homeworks': [90, 85, 0, 80, 90]}
}

# iterates through class_grades dict for each element check
final_grades = dict()
for i in class_grades:
    test_points = homework_points = 0
    
    # uses min function to calculate lowest value in homeworks key
    homeworks = class_grades[i]['homeworks']
    if homeworks:
        homeworks.remove(min(homeworks))  # drops lowest score

    # applies 25% weightage for each test
    for j in class_grades[i]['tests']:
        test_points += j * 0.25

    # applies new weightage of 12.5% for each homework
    for j in homeworks:
        homework_points += j * 0.125

    final_grades[i] = round(test_points + homework_points, 0)

for i in final_grades: # iterating through dict to print each element with associated key
    print(i, final_grades[i])

# script should only be outputting final grades - individual homework and test pointages found in wrkst_charts