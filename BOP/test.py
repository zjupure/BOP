#!user/bin/python
import requests
import time

BASE_URL = "http://echobop.chinacloudapp.cn/BOP/route"

# add id to the lists
# first five are official test cases
idLists = [{"id1": 2251253715, "id2":2180737804},
    {"id1": 2147152072, "id2":189831743},
    {"id1": 2332023333, "id2":2310280492},
    {"id1": 2332023333, "id2":57898110},
    {"id1": 57898110, "id2":2014261844}]
    #other test cases
    #{"id1": 189831743, "id2": 2147152072},
    #{"id1": 621499171, "id2": 2100837269},
    #{"id1": 2027775552, "id2": 2085680244},
    #{"id1": 2100837269, "id2": 621499171},
    #{"id1": 2126125555, "id2": 2153635508},
    #{"id1": 2126701683, "id2": 621499171},
    #{"id1": 2126701683, "id2": 2091907464},
    #{"id1": 2131087226, "id2": 2179036997},
    #{"id1": 2143554828, "id2": 2099495348},
    #{"id1": 2153635508, "id2": 2126125555},
    #{"id1": 2175015405, "id2": 2121939561},
    #{"id1": 2179036997, "id2": 2131087226},
    #{"id1": 2179036997, "id2": 2152770371},
    #{"id1": 2180737804, "id2": 2251253715},
    #{"id1": 2268927867, "id2": 2179036997},
    #{"id1": 2292217923, "id2": 2100837269}]

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

        print("elapse time: " + str(consume) + "\n")
        # assure the results are all corrected
        score += (1-consume/300)
        cnt += 1

        #print(rsp + "\n")
        time.sleep(5);
    #score *= 100/cnt/99
    print(score)



if __name__ == '__main__':
    autorun()
