#!user/bin/python
import requests
import time

BASE_URL = "http://echobop.chinacloudapp.cn/BOP/route"

# add id to the lists
idLists = [{"id1": 2251253715, "id2":2180737804},
    {"id1": 2147152072, "id2":189831743},
    {"id1": 2332023333, "id2":2310280492},
    {"id1": 2332023333, "id2":57898110},
    {"id1": 57898110, "id2":2014261844}]

def get(id1, id2):
    payload = {'id1' : id1, 'id2' : id2}
    r = requests.get(BASE_URL, params=payload)

    return r.text

def autorun():
    cnt = 0
    score = 0
    for ids in idLists:
        start_time = time.time()
        rsp = get(ids['id1'], ids['id2'])
        end_time = time.time()
        consume = end_time - start_time

        # assure the results are all corrected
        score += (1-consume/300)
        cnt += 1

        print(rsp)
        print("\n")
    #score *= 100/cnt/99
    print(score)


if __name__ == '__main__':
    autorun()
