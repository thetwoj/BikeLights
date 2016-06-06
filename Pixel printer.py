#!/usr/bin/env python

from Tkinter import *
from Tkinter import Tk
from tkFileDialog import askopenfilename
from PIL import Image
from math import sqrt, atan, pi
import time

NUM_LEDs = 24
height_buff = NUM_LEDs/10
final_height = NUM_LEDs + height_buff

seedfile = ''
outputfile = ''

def get_pixels():

    root = Tk()
    root.title("Pixel Printer")
    root.grid()

    inner_top = Frame(root)
    inner_top.grid(row=0, column=0)

    t1 = Entry(inner_top, width = 40)
    t2 = Entry(inner_top, width = 40)

    v = StringVar()

    root.columnconfigure(0, weight=1)
    root.columnconfigure(1, weight=1)

    b1 = Button(inner_top, text = "Source")
    b2 = Button(inner_top, text = "Dest")
    b3 = Button(inner_top, text = "Calc")

    b1.configure(command= lambda:get_source(t1))
    b2.configure(command= lambda:get_dest(t2))
    b3.configure(command= lambda:calculate(t1, t2, v))

    t1.grid(row=0, column=0)
    b1.grid(row=0, column=1, sticky=E+W)
    t2.grid(row=1, column=0)
    b2.grid(row=1, column=1, sticky=E+W)
    b3.grid(row=2, column=1, sticky=E+W)
    
    root.mainloop()

	
def get_source(t1):
    seedfile = askopenfilename()
    t1.delete(0, END)
    t1.insert(0, seedfile)

	
def get_dest(t2):
    outputfile = askopenfilename()
    t2.delete(0, END)
    t2.insert(0, outputfile)

	
def calculate(t1, t2, v):
    if(t1.get() == "" or t2.get() == ""):
        return

    seedfile = t1.get()
    outputfile = t2.get()

    im = Image.open(seedfile)
    pix = im.load()
    wid, hi = im.size

    if(wid != hi):
        scribe_pixels(t1, t2, v)
        return

    r_total = '{'
    g_total = '{'
    b_total = '{'

    tempim = Image.new('RGB', (180,24), "white")
    temppix = tempim.load()

    output = open(outputfile, 'w')

    half = wid/2

    for y in range (wid-1, 0, -1):
        
        for x in range (0, wid):
            r, g, b = pix[x,y]
            
            y_cord = half - y
            x_cord = x - half
            
            y_new = sqrt(x_cord**2 + y_cord**2) * (float(final_height)/float(half))

            if x_cord > 0 and y_cord >= 0:
                x_new = atan(float(y_cord)/float(x_cord))
            elif x_cord < 0 and y_cord >= 0:
                x_new = atan(float(y_cord)/float(x_cord)) + pi
            elif x_cord < 0 and y_cord <= 0:
                x_new = atan(float(y_cord)/float(x_cord)) + pi
            elif x_cord > 0 and y_cord <= 0:
                x_new = atan(float(y_cord)/float(x_cord)) + 2*pi
            
            if y_new < NUM_LEDs and x_cord != 0:
                temppix[(179-(int(x_new * ((180)/(2*pi))))), (NUM_LEDs-y_new)] = (r, g, b)

    tempim.save('temp.bmp')
    scribe_pixels(t1, t2, v, 'C:/Users/JJ/Desktop/temp.bmp')


def scribe_pixels(t1, t2, v, seed = ''):

    if seed == '':
        seedfile = t1.get()
    else:
        seedfile = seed
        
    outputfile = t2.get()

    r_total = '{'
    g_total = '{'
    b_total = '{'

    im = Image.open(seedfile)

    tempim = Image.new('RGB', (104,26), "white")
    temppix = tempim.load()

    output = open(outputfile, 'w')

    pix = im.load()
    wid, hi = im.size

    for y in range (0, hi):
        for x in range (0, wid):    
            r, g, b = pix[x,y]
            output.write("{%s,%s,%s}\n" % (r, g, b))
        
          
if __name__ == '__main__':
    
    get_pixels()
