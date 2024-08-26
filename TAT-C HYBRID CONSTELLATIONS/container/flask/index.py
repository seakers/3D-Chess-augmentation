from flask             import Flask, render_template, url_for, request, jsonify, redirect
from dash.dependencies import Input, Output
from collections       import OrderedDict
import threading
import os
import sys
import socket
import json
import pprint
import pandas               as pd
import pygmo                as pg
import plotly.graph_objs    as go
import numpy                as np
import dash
import dash_table
import dash_core_components as dcc
import dash_html_components as html
import dash_bootstrap_components as dbc



from app       import app
from dashboard import tab_1, tab_2, tab_3


NASA_LOGO = "https://www.nasa.gov/sites/default/files/thumbnails/image/nasa-logo-web-rgb.png"


# ------------------------ DASH OBJECTS ------------------------ #
navbar = dbc.Navbar(
    [
        html.A(
            # Use row and col to control vertical alignment of logo / brand
            dbc.Row(
                [
                    dbc.Col(dbc.NavbarBrand("Tradespace Analysis Tool", className="ml-2")),
                ],
                justify="between",
                align="center",
                no_gutters=True,
            ),
        ),
        dbc.NavbarToggler(id="navbar-toggler"),
    ],
    color="#2a3f5f",
    dark=True,
)

label_color = {"color": "#2a3f5f", 'border': 'none'}
tab_style = {
    'paddingTop': '1px',
    'height': '45px',
}

pages = html.Div(
    [
        dcc.Tabs(
            [
                dcc.Tab(label="Overview",              value="tab-1"),
                dcc.Tab(label="Compare Architectures", value="tab-2"),
                dcc.Tab(label="Architecture Analysis", value="tab-3"),
            ],
            id="tabs",
            style=tab_style,
        ),
        html.Div(id="content",className='content-fixed'),
    ]
)
# ------------------------ DASH OBJECTS ------------------------ #




#@app.callback(Output("content", "children"), [Input("tabs", "active_tab")])



# ------------------------ DASH MASTER START ------------------------ #
app.layout = html.Div([navbar, pages])
@app.callback(Output("content", "children"), [Input("tabs", "value")])
def switch_tab(at):
	if(at == 'tab-1'):
		return tab_1.layout
	elif(at == 'tab-2'):
		return tab_2.layout
	elif(at == 'tab-3'):
		return tab_3.layout
	return tab_1.layout
# ------------------------ DASH MASTER END ------------------------ #







# ---------- URL Routing ---------- #

# THREAD FUNCTION
def run_tatc_thread():
	print("TAT-C Thread Started")
	os.chdir("/tat-c")
	os.system("python ./tse/bin/tse.py /mission/mission.json /mission")
	print("TAT-C Thread Completed")


@app.server.route("/")
def main_page():
	print("Rendering Main Page")
	return render_template('index.html')


# Get build run data and run tool
@app.server.route("/getRunFiles", methods=['POST'])
def getRunFiles():
	os.system("rm -rf /mission/*")
	json_files = request.get_json()
	mission_file = json_files["mission"]

	with open('/mission/mission.json', 'w') as fp:
	 	json.dump(mission_file, fp)


	# Run TAT-C
	#os.chdir("/tat-c")
	#os.system("python ./tse/bin/tse.py /mission/mission.json /mission")
	print("Running Tool")
	tatc_thread = threading.Thread(target=run_tatc_thread)
	tatc_thread.start()


	# Now we will route to the dash application
	return redirect( url_for('/data/'))

if __name__ == "__main__":
	app.run_server(host='0.0.0.0', port=80, debug=True)
# ---------- URL Routing ---------- #
