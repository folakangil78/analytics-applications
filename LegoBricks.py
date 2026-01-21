lego_height = 9.6 #mm
duplo_height = 19.2 #mm
x = 3 #building tower that is at least 3m high
desired_tower_height = x * 1000 #mm = 3000mm

#1
#dividing total height by height of bricks to find total required
print('Number of required lego bricks: ', (desired_tower_height // lego_height) + 1)
print('Number of required duplo bricks: ', (desired_tower_height // duplo_height) + 1)
#need to floor result and add one to ensure that requirement of bricks are full legos, not a part of a lego

#2
# while loop to iterate until 3000 and increment number of bricks throughout
legos_height = 0
brick_count = 0
while legos_height < 3000:
    brick_count += 1
    legos_height+=lego_height
print('Number of lego bricks needed to get to >3000mm tall tower with while loop: ', brick_count)

#3
#while loop to calculate how many duplos to reach 3000
# making sure to start iterator at 0, previously made mistake of starting iteration at initial height of single duplo brick,
# which misses count by 1
bricks_count = 0
duplos_height = 0
while duplos_height < 3000:
    bricks_count +=1
    duplos_height+=duplo_height
print('Number of duplo bricks to build tower: ', bricks_count)
