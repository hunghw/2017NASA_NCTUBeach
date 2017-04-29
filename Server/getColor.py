
#path = 'A2017118.L3m_DAY_CHL_chlor_a_4km.nc.png'
#path2 = 'CHLO_colorscale.png'

from PIL import Image

def check_dang( in_rgb ):
    if in_rgb[0]>50 and in_rgb[1]==0 and in_rgb[2]==0:
        return 1;
    elif in_rgb[0]==255 and in_rgb[1]<193 and in_rgb[2] == 0:
        return 1;
    return 0;

def getColor(lat, log):
    path = 'A2017118.L3m_DAY_CHL_chlor_a_4km.nc.png'
    file_data = Image.open(path)
    file_data = file_data.convert('RGB') # conversion to RGB
    data = file_data.load()
    x = 0
    y = 0
    if lat < 0:
        y = int(round( 2159*(lat+90)/90))
    else:
        y = int(round(2159/90*lat+2160))

    if log <0:
        x = int(round(4319*(log+180)/180))
    else:
        x = int(round(4319/180*log+4320))
    
    value_dang = 0;
    for i in range(x-5,x+5):
        if i>=0 and i<8640:
            for j in range (y-5,y+5):
                if j>=0 and j<4320:
                    value_dang = value_dang +  check_dang( data[i,j] )
    #print x,y
    #print check_dang( data[lat,log] )
    return value_dang
