"""Generate original, street-connected village annexes using vanilla blocks. No game launch."""
from pathlib import Path
import gzip,struct,json,zipfile
ROOT=Path(__file__).resolve().parents[1]
RESOURCE=ROOT/'src/main/resources'
def payload(t,v):
    if t==3:return struct.pack('>i',v)
    if t==8:
        b=v.encode('utf-8');return struct.pack('>H',len(b))+b
    if t==9:
        sub,values=v;return bytes([sub])+struct.pack('>i',len(values))+b''.join(payload(sub,a) for a in values)
    if t==10:return b''.join(bytes([sub])+payload(8,name)+payload(sub,value) for name,(sub,value) in v.items())+b'\0'
    raise ValueError(t)
def ints(v):return (9,(3,v))
def strings(v):return (10,{k:(8,value) for k,value in v.items()})
def structure(kind):
    blocks={};palette=[]
    def place(x,y,z,name,props=None,nbt=None):
        state={'Name':(8,'minecraft:'+name)}
        if props:state['Properties']=strings(props)
        if state not in palette:palette.append(state)
        b={'pos':ints([x,y,z]),'state':(3,palette.index(state))}
        if nbt:b['nbt']=strings(nbt)
        blocks[x,y,z]=b
    def box(x0,y0,z0,x1,y1,z1,name):
        for x in range(x0,x1+1):
            for y in range(y0,y1+1):
                for z in range(z0,z1+1):place(x,y,z,name)
    box(0,1,0,12,12,14,'air');box(0,0,0,12,0,14,'dirt');box(5,0,0,7,0,14,'dirt_path')
    if kind=='watchtower':
        box(2,0,5,10,0,13,'cobblestone')
        for x in [3,9]:
            for z in [6,12]:box(x,1,z,x,9,z,'oak_log')
        box(3,7,6,9,7,12,'oak_planks');box(2,10,5,10,10,13,'oak_slab')
        for x in range(3,10):place(x,8,6,'oak_fence');place(x,8,12,'oak_fence')
        for z in range(6,13):place(3,8,z,'oak_fence');place(9,8,z,'oak_fence')
        box(6,1,13,6,7,13,'oak_log')
        for y in range(1,8):place(6,y,12,'ladder',{'facing':'north','waterlogged':'false'})
        place(6,9,9,'lantern',{'hanging':'true','waterlogged':'false'})
    elif kind=='ruins':
        box(2,0,5,10,0,13,'mossy_cobblestone')
        for x in range(2,11):box(x,1,13,x,1+(x%3),13,'stone_bricks')
        for z in range(6,13):box(2,1,z,2,1+(z%3),z,'mossy_stone_bricks')
        box(9,1,10,10,4,11,'cracked_stone_bricks');place(6,1,10,'chiseled_stone_bricks');place(6,2,10,'torch')
        place(4,1,8,'gravel');place(8,1,12,'gravel')
    else:
        box(2,0,5,10,0,13,'oak_planks')
        for x in [2,10]:box(x,1,6,x,4,6,'oak_fence');box(x,1,12,x,4,12,'oak_fence')
        box(2,5,6,10,5,12,'white_wool');box(5,5,6,7,5,12,'yellow_wool')
        box(3,1,10,9,1,11,'oak_planks');place(4,2,10,'crafting_table');place(8,2,10,'fletching_table')
        place(3,1,7,'hay_block');place(4,1,7,'hay_block');place(9,1,7,'lantern',{'hanging':'false','waterlogged':'false'})
    place(6,0,0,'jigsaw',{'orientation':'north_up'},{'id':'minecraft:jigsaw','name':'minecraft:building_entrance','target':'minecraft:building_entrance','pool':'minecraft:empty','joint':'aligned','final_state':'minecraft:dirt_path'})
    root={'DataVersion':(3,4556),'size':ints([13,13,15]),'palette':(9,(10,palette)),'blocks':(9,(10,list(blocks.values()))),'entities':(9,(10,[]))}
    dest=RESOURCE/f'data/oxyrpg/structure/village/{kind}.nbt';dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(gzip.compress(bytes([10])+payload(8,'')+payload(10,root),mtime=0))
for kind in ['watchtower','ruins','waystation']:structure(kind)
