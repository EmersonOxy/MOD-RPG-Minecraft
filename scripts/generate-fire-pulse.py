from pathlib import Path
import struct,zlib,json
root=Path(__file__).resolve().parents[1]/"src/main/resources/assets/oxyrpg"
folder=root/"textures/particle";folder.mkdir(parents=True,exist_ok=True)
def chunk(name,data):return struct.pack(">I",len(data))+name+data+struct.pack(">I",zlib.crc32(name+data)&0xffffffff)
for frame in range(16):
 radius=1.8+frame*.82;rows=[];rects=[]
 for y in range(32):
  row=bytearray()
  for x in range(32):
   distance=((x-15.5)**2+(y-15.5)**2)**.5
   inside=abs(distance-radius)<=1.05
   shade=255 if distance>=radius else 205
   row.extend((shade,shade,shade,255) if inside else (0,0,0,0))
   if inside:rects.append(f'<rect x="{x}" y="{y}" width="1" height="1" fill="rgb({shade},{shade},{shade})"/>')
  rows.append(b"\0"+row)
 image=b"\x89PNG\r\n\x1a\n"+chunk(b"IHDR",struct.pack(">IIBBBBB",32,32,8,6,0,0,0))+chunk(b"IDAT",zlib.compress(b"".join(rows)))+chunk(b"IEND",b"")
 (folder/f"fire_ring_{frame}.png").write_bytes(image)
 sources=root/"vector/particles";sources.mkdir(parents=True,exist_ok=True)
 (sources/f"fire_ring_{frame}.svg").write_text('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" shape-rendering="crispEdges">'+"".join(rects)+"</svg>\n")
(root/"particles/fire_pulse.json").write_text(json.dumps({"textures":[f"oxyrpg:fire_ring_{i}" for i in range(16)]})+"\n")
