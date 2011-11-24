make

#python tools/playgame.py "java -jar MyBot.jar" "python tools/sample_bots/python/HunterBot.py" --map_file tools/maps/random_walk/random_walk_02p_01.map --log_dir game_logs --turns 60 --scenario --food none --player_seed 7 --verbose -e
#python "tools/playgame.py" --engine_seed 42 --player_seed 42 --turntime 500 --end_wait=0.25 --verbose --log_dir game_logs --turns 250 --map_file "tools/maps/random_walk/random_walk_04p_01.map" "python ""tools/sample_bots/python/GreedyBot.py""" "java -jar MyBot.jar" "python ""tools/sample_bots/python\HunterBot.py""" "python ""tools/sample_bots/python/HunterBot.py"""
python "tools/playgame.py" --engine_seed 42 --player_seed 42 --turntime 500 --end_wait=0.25 --verbose --log_dir game_logs --turns 1050 --map_file "tools/maps/maze/maze_02p_01.map" "java -jar MyBot.jar" "python ""tools/sample_bots/python/GreedyBot.py""" -e
