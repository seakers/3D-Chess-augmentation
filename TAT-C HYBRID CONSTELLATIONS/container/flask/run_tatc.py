# This is the file that will initiate the running of TAT-C
# We will have to run the GUI from this file

from flask import Flask, render_template, url_for, request, jsonify, redirect
from dash.dependencies import Input, Output
from collections import OrderedDict 

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



# ------------------------ GLOBAL VARIABLES ------------------------ #
architectures   = []
arch_json_files = {}
globalFiles     = []
localFiles      = []
costFiles       = []
poiFile         = []

left_button_clicks   = None
middle_button_clicks = None
right_button_clicks  = None


overview_data = OrderedDict(
	[
		("Architecture", ["arch-0", "arch-1", "arch-2", "arch-3", "arch-4", "arch-5"]),
		("Revisit", ["20", "30", "40", "50", "60", "70"])
	]
	)
data_frame = pd.DataFrame(overview_data)

# ------------------------ GLOBAL VARIABLES ------------------------ #







# ------------------------ CREATE APPLICATION ------------------------ #
tradespace_analysis_tool = Flask(__name__, static_folder='/flask/static', static_url_path="/flask/static", template_folder='/flask/template')
app = dash.Dash(__name__, external_stylesheets=[dbc.themes.BOOTSTRAP], server=tradespace_analysis_tool, url_base_pathname='/data/', assets_url_path='/flask/assets')
app.title = "TAT-C"
app.config['suppress_callback_exceptions']=True
#app.css.append_css({'external_url': 'https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css'})
# ------------------------ CREATE APPLICATION ------------------------ #








# ------------------------ FLASK ------------------------ #
def setGlobals():
	left_button_clicks   = None
	middle_button_clicks = None
	right_button_clicks  = None
	rootdir  = '/mission'
	#poiFile  = pd.read_csv('/mission/cache/POI.csv')
	for subdir, dirs, files in os.walk(rootdir):
		for d in sorted(dirs):
			if(d[:4] != 'arch'):
				break
			archdir = rootdir + '/' + str(d)
			gbl_temp = archdir + '/gbl.json'
			lcl_temp = archdir + '/lcl.csv'
			cost_temp = archdir + '/CostRisk_Output.json'
			arch_temp = archdir + '/arch.json'
			architectures.append(str(d))
			localFiles.append(pd.read_csv(lcl_temp))
			globalFiles.append(pd.read_json(gbl_temp))
			arch_json_files[str(d)] = pd.read_json(arch_temp)
			data = json.load(open(cost_temp))
			costFiles.append(data['lifecycleCost'])



@tradespace_analysis_tool.route("/")
def main_page():
	print("Rendering Main Page")
	return render_template('index.html')


@tradespace_analysis_tool.route("/getRunFiles", methods=['POST'])
def getRunFiles():
	os.system("rm -rf /mission/*")
	json_files = request.get_json()
	mission_file = json_files["mission"]

	with open('/mission/mission.json', 'w') as fp:
	 	json.dump(mission_file, fp)


	# Run TAT-C
	print("Runnint Tool")
	os.chdir("/tat-c")
	os.system("python ./tse/bin/tse.py /mission/mission.json /mission")


	# Here we will set our global variables for the dash app
	setGlobals()

	# Now we will route to the dash application
	return redirect( url_for('/data/'))
# ------------------------ FLASK ------------------------ #














# ------------------------ DASH OBJECTS ------------------------ #
navbar = dbc.Navbar(
    [
        html.A(
            # Use row and col to control vertical alignment of logo / brand
            dbc.Row(
                [
                    dbc.Col(dbc.NavbarBrand("Tradespace Analysis Tool for Constellations", className="ml-2")),
                ],
                align="center",
                no_gutters=True,
            ),
            href="https://plot.ly",
        ),
        dbc.NavbarToggler(id="navbar-toggler"),
    ],
    color="#2a3f5f",
    dark=True,
)



label_color = {"color": "#2a3f5f", 'border': 'none'}
pages = html.Div(
    [
        dbc.Tabs(
            [
                dbc.Tab(label="Overview", tab_id="tab-1", tabClassName='dashboard-tab', label_style=label_color),
                dbc.Tab(label="Compare Architectures", tab_id="tab-2", tabClassName='dashboard-tab', label_style=label_color),
                dbc.Tab(label="Architecture Analysis", tab_id="tab-3", tabClassName='dashboard-tab', label_style=label_color),
            ],
            id="tabs",
            active_tab="tab-1",
        ),
        html.Div(id="content"),
    ]
)




gbl_values = ["ResponseTime", "TimeToCoverage", "DataLatency", "AccessTime", "DownlinkTimePerPass", "NumOfPOIpasses", "RevisitTime"]
cost_values = ["Cost"]
interface_padding={'padding-left': '10px', 'padding-right': '10px'}
interface_input_padding={'padding-left': '18px'}




# ---------------- TAB ONE LAYOUT START ----------------
tab_one = html.Div(
	[
	


	)
		dbc.Row(
		children=[

			# Left large column
			dbc.Col(
			children=[

				# Plot
				dbc.Row(
				children=[
					dbc.Col(
					children=[
						html.Div(
						children=[
							dcc.Graph(id='overview-graph')
						],
						className='overview-chart-layout',
						),
					],
					width=12,
					className='',
					)

				],
				no_gutters=True,
				className='',
				),


				# Table
				dbc.Row(
				children=[
					dbc.Col(
					children=[
						html.Div(
						children=[
							dash_table.DataTable(
						    style_cell={'textAlign': 'left'},
						    style_as_list_view=False,
						    style_header={
						        'backgroundColor': 'white',
						        'fontWeight': 'bold'
						    },
						    id='overview-table',
						    sorting=True,
						    row_selectable='multi',
							) 
						],
						className='overview-table-layout text-center',
						),
					],
					width=12,
					className='',
					)

				],
				no_gutters=True,
				className='',
				),



			],
			width=9,
			className='',
			),



			# Overview Chart Interface -- START
			dbc.Col(
			children=[
				html.Div(children=[

		    		# OBJECTIVES
		    		dbc.FormGroup(
					    [
					        dbc.Label("Objectives", className='interface-font-lg'),
					        dbc.Checklist(
					            options=[
					            	{'label': i, 'value': i} for i in (gbl_values + cost_values)
					            ],
					            values=['Cost', 'RevisitTime'],
					            id="checklist-input",
					            className='text-left checklist-format interface-font-sm',
					            labelStyle=interface_input_padding,
					        ),
					    ], className='objectives-format'
					),



		    		# CHART TYPE
					dbc.FormGroup(
					    [
					        dbc.Label("Data Points", className='interface-font-lg'),
					        dbc.RadioItems(
				                options=[
				                	{'label': 'All Architectures', 'value': 'all'},
				                	{'label': 'Pareto Front', 'value': 'pareto'},
				                ],
				                value='all',
				                id='overview-plot-type',
				                className='text-left radio-format interface-font-sm',
				                labelStyle=interface_input_padding,
				            ),
					    ], className='chart-type-format'
					),


					# SPECIFY AXIS
					dbc.FormGroup(
					    [
					        dbc.Label("Specify Axis", className='interface-font-lg'),

					        dbc.FormGroup(
							    [
							        dbc.Label("X", width=2),
							        dbc.Col(
							            dcc.Dropdown(
										    options=[],
										    value='',
										    id='x-axis-dropdown'
										)  ,
							            width=10,
							        ),
							    ],
							    row=True,
							    className='specify-axis-format'
							),
							dbc.FormGroup(
							    [
							        dbc.Label("Y", width=2),
							        dbc.Col(
							            dcc.Dropdown(
										    options=[],
										    value='',
										    id='y-axis-dropdown'
										)  ,
							            width=10,
							        ),
							    ],
							    row=True,
							    className='specify-axis-format'
							),
							dbc.FormGroup(
							    [
							        dbc.Label("Z", width=2),
							        dbc.Col(
							            dcc.Dropdown(
										    options=[],
										    value='',
										    id='z-axis-dropdown'
										)  ,
							            width=10,
							        ),
							    ],
							    row=True,
							    className='specify-axis-format'
							),
					    ], 
					    className='chart-type-format interface-font-sm'
					),


					# RENDER GRAPH BUTTON
					dbc.FormGroup(
					    [
					        dbc.Button("Render Plot", block=True, className='overview-submit-btn', id='render-button')
					    ], className='interface-submit-format'
					),

		            ], className='overview-chart-interface-layout text-center'
		    	)

			],
			width=3,
			className='',
			),
			# Overview Chart Interface -- END



		],
		no_gutters=True,
		className='',
		)

	],
)
# ---------------- TAB ONE LAYOUT END ----------------





# ---------------- TAB ONE CALLBACKS START ----------------

# CHOOSE X AXIS
@app.callback(
	dash.dependencies.Output('x-axis-dropdown', 'options'),
	[dash.dependencies.Input('checklist-input', 'values'),
	 dash.dependencies.Input('y-axis-dropdown', 'value'),
	 dash.dependencies.Input('z-axis-dropdown', 'value')])
def change_x_axis_options(values, y_value, z_value):
	options = []
	for value in values:
		if(y_value != value and z_value != value):
			options.append({'label': value, 'value': value})
	return options




# CHOOSE Y AXIS
@app.callback(
	dash.dependencies.Output('y-axis-dropdown', 'options'),
	[dash.dependencies.Input('checklist-input', 'values'),
	 dash.dependencies.Input('x-axis-dropdown', 'value'),
	 dash.dependencies.Input('z-axis-dropdown', 'value')])
def change_y_axis_options(values, x_value, z_value):
	options = []
	for value in values:
		if(x_value != value and z_value != value):
			options.append({'label': value, 'value': value})
	return options




# CHOOSE Z AXIS
@app.callback(
	dash.dependencies.Output('z-axis-dropdown', 'options'),
	[dash.dependencies.Input('checklist-input', 'values'),
	 dash.dependencies.Input('x-axis-dropdown', 'value'),
	 dash.dependencies.Input('y-axis-dropdown', 'value')])
def change_z_axis_options(values, x_value, y_value):
	options = []
	if(len(values) < 3):
		return options
	
	for value in values:
		if(x_value != value and y_value != value):
			options.append({'label': value, 'value': value})
	return options




# RENDER GRAPH CALLBACK
@app.callback(
	[dash.dependencies.Output('overview-graph', 'figure'),
	 dash.dependencies.Output('overview-table', 'columns'),
	 dash.dependencies.Output('overview-table', 'data'),],
	[dash.dependencies.Input('render-button', 'n_clicks')],
	[dash.dependencies.State('checklist-input', 'values'),
	 dash.dependencies.State('overview-plot-type', 'value'),
	 dash.dependencies.State('x-axis-dropdown', 'value'),
	 dash.dependencies.State('y-axis-dropdown', 'value'),
	 dash.dependencies.State('z-axis-dropdown', 'value')])
def render_graph(n_clicks, objectives, plot_type, x_axis, y_axis, z_axis):
	print(n_clicks)
	print(objectives)
	print(len(objectives))
	print(plot_type)
	print(x_axis)
	print(y_axis)
	print(z_axis)
	if(len(objectives) < 2):
		return [],[],[]
	if(plot_type == 'all'):
		return create_overview_all(objectives, x_axis, y_axis, z_axis), get_table_columns(objectives), get_table_data(objectives)
	elif(plot_type == 'pareto'):
		return create_overview_pareto(objectives, x_axis, y_axis, z_axis), get_table_columns(objectives, True), get_table_data(objectives, True)



def create_overview_all(objectives, x_axis, y_axis, z_axis):
	if(len(objectives) is 2):
		x_data = get_data(x_axis)
		y_data = get_data(y_axis)
		return {
		    'data': [
		        go.Scatter(
		        	x=x_data,
		        	y=y_data,
		        	text=architectures,
		        	mode='markers',
		        	marker = dict(
		        		size = 10,
		        		line = dict(
		        			width = 1,
		        			)
		        		)
		        )
		    ],
		    'layout': go.Layout(
		        xaxis=dict(
		        	title=x_axis,
		        	showgrid=True,
			        zeroline=True,
			        showline=True,
			        gridcolor='#bdbdbd',
		        	),
				yaxis=dict(
		        	title=y_axis,
		        	showgrid=True,
			        zeroline=True,
			        showline=True,
			        gridcolor='#bdbdbd',
		        	),
				hovermode='closest',
				font=dict(size=13, color='#2a3f5f'),
				title='Global Metrics',
		    )
		}
	elif(len(objectives) is 3):
		x_data = get_data(x_axis)
		y_data = get_data(y_axis)
		z_data = get_data(z_axis)
		return {
		    'data': [
		        go.Scatter(
		        	x=x_data,
		        	y=y_data,
		        	text=architectures,
		        	mode='markers',
		        	marker = dict(
		        		size = 10,
		        		color = z_data,
		        		colorscale='Viridis',
		        		showscale=True,
		        		colorbar=dict(
		        			title=z_axis),
		        		line = dict(
		        			width = 1,
		        			)
		        		)
		        )
		    ],
		    'layout': go.Layout(
		        xaxis=dict(
		        	title=x_axis,
		        	showgrid=True,
			        zeroline=True,
			        showline=True,
			        gridcolor='#bdbdbd',
		        	),
				yaxis=dict(
		        	title=y_axis,
		        	showgrid=True,
			        zeroline=True,
			        showline=True,
			        gridcolor='#bdbdbd',
		        	),
				hovermode='closest',
				font=dict(size=13, color='#2a3f5f'),
				title='Global Metrics',
		    )
		}


def create_overview_pareto(objectives, x_axis, y_axis, z_axis):
	np.set_printoptions(formatter={'float': lambda x: "{0:0.3f}".format(x)})
	data = []
	for objective in objectives:
		data.append(get_data(objective))

	num_data_points = (len(data[0]) - 1)
	all_data = []
	for x in range(0,num_data_points):
		temp = []
		for obj_data in data:
			temp.append(obj_data[x])
		all_data.append(temp)

	#ndf, dl, dc, ndr = pg.fast_non_dominated_sorting(points = all_data)
	non_dominated = is_pareto_efficient(np.array(all_data))
	print(non_dominated)

	if(len(objectives) is 2):
		x_data = []
		y_data = []
		pareto_arch = []
		x_data_all = get_data(x_axis)
		y_data_all = get_data(y_axis)
		for x in range(0, (len(x_data_all) - 1) ):
			if(non_dominated[x] == True):
				x_data.append(x_data_all[x])
				y_data.append(y_data_all[x])
				pareto_arch.append(architectures[x])

		return {
		    'data': [
		        go.Scatter(
		        	x=x_data,
		        	y=y_data,
		        	text=pareto_arch,
		        	mode='markers',
		        	marker = dict(
		        		size = 10,
		        		line = dict(
		        			width = 1,
		        			)
		        		)
		        )
		    ],
		    'layout': go.Layout(
		        xaxis=dict(
		        	title=x_axis,
		        	showgrid=True,
			        zeroline=True,
			        showline=True,
			        gridcolor='#bdbdbd',
		        	),
				yaxis=dict(
		        	title=y_axis,
		        	showgrid=True,
			        zeroline=True,
			        showline=True,
			        gridcolor='#bdbdbd',
		        	),
				hovermode='closest',
				font=dict(size=13, color='#2a3f5f'),
				title='Global Metrics',
		    )
		}
	elif(len(objectives) is 3):
		x_data = []
		y_data = []
		z_data = []
		pareto_arch = []
		x_data_all = get_data(x_axis)
		y_data_all = get_data(y_axis)
		z_data_all = get_data(z_axis)
		for x in range(0, (len(x_data_all) - 1) ):
			if(non_dominated[x] == True):
				x_data.append(x_data_all[x])
				y_data.append(y_data_all[x])
				z_data.append(z_data_all[x])
				pareto_arch.append(architectures[x])
		return {
		    'data': [
		        go.Scatter(
		        	x=x_data,
		        	y=y_data,
		        	text=pareto_arch,
		        	mode='markers',
		        	marker = dict(
		        		size = 10,
		        		color = z_data,
		        		colorscale='Viridis',
		        		showscale=True,
		        		colorbar=dict(
		        			title=z_axis),
		        		line = dict(
		        			width = 1,
		        			)
		        		)
		        )
		    ],
		    'layout': go.Layout(
		        xaxis=dict(
		        	title=x_axis,
		        	showgrid=True,
			        zeroline=True,
			        showline=True,
			        gridcolor='#bdbdbd',
		        	),
				yaxis=dict(
		        	title=y_axis,
		        	showgrid=True,
			        zeroline=True,
			        showline=True,
			        gridcolor='#bdbdbd',
		        	),
				hovermode='closest',
				font=dict(size=13, color='#2a3f5f'),
				title='Global Metrics',
		    )
		}
	return "TO_DO"


def is_pareto_efficient(costs):
    """
    :param costs: An (n_points, n_costs) array
    :return: A (n_points, ) boolean array, indicating whether each point is Pareto efficient
    """
    is_efficient = np.ones(costs.shape[0], dtype = bool)
    for i, c in enumerate(costs):
        if is_efficient[i]:
            is_efficient[is_efficient] = np.any(costs[is_efficient]<c, axis=1)  # Keep any point with a lower cost
            is_efficient[i] = True  # And keep self
    return is_efficient




def get_table_columns(objectives, pareto=False):
	values = ["ResponseTime", "TimeToCoverage", "AccessTime", "DownlinkTimePerPass", "RevisitTime"]
	data = dict()
	data["Architecture"] = architectures
	for obj in values:
		data[obj] = get_data(obj)

	data_fr = pd.DataFrame(data)
	return [{'id': c, 'name': c} for c in data_fr.columns]

def get_table_data(objectives, pareto=False):
	values = ["ResponseTime", "TimeToCoverage", "AccessTime", "DownlinkTimePerPass", "RevisitTime"]
	np.set_printoptions(formatter={'float': lambda x: "{0:0.3f}".format(x)})
	data = []
	for objective in objectives:
		data.append(get_data(objective))

	num_data_points = (len(data[0]) - 1)
	all_data = []
	for x in range(0,num_data_points):
		temp = []
		for obj_data in data:
			temp.append(obj_data[x])
		all_data.append(temp)

	#ndf, dl, dc, ndr = pg.fast_non_dominated_sorting(points = all_data)
	non_dominated = is_pareto_efficient(np.array(all_data))

	if(pareto == True):
		data = dict()
		temp_arch = []
		for obj in values:
			temp = get_data(obj)
			temp_add = []
			for x in range(0, (len(temp)-1) ):
				if(non_dominated[x] == True):
					temp_add.append(temp[x])
			data[obj] = temp_add

		for x in range(0, (len(architectures)-1) ):
			if(non_dominated[x] == True):
				temp_arch.append(architectures[x])
		data["Architecture"] = temp_arch

		data_fr = pd.DataFrame(data)
		return data_fr.to_dict('records')
	else:
		data = dict()
		data["Architecture"] = architectures
		for obj in values:
			data[obj] = get_data(obj)
		data_fr = pd.DataFrame(data)
		return data_fr.to_dict('records')









def get_data(axis):
	axis_data = []
	if(axis == 'Cost'):
		for df in costFiles:
			axis_data.append( (df['estimate'] / 1000000) )
	else:
		for df in globalFiles:
				axis_data.append(df[axis]['avg'])
	return axis_data
# ---------------- TAB ONE CALLBACKS END ----------------












tab_two   = "tab 2"
tab_three = "tab 3"














# ------------------------ DASH MASTER START ------------------------ #
app.layout = html.Div([navbar, pages])


@app.callback(Output("content", "children"), [Input("tabs", "active_tab")])
def switch_tab(at):
	if(at == 'tab-1'):
		return tab_one
	elif(at == 'tab-2'):
		return tab_two
	elif(at == 'tab-3'):
		return tab_three

	return tab_one

# ------------------------ DASH MASTER END ------------------------ #



















# @tradespace_analysis_tool.route("/data")
# def data():
# 	print("Rendering Results Page")
# 	return render_template('results.html')


if __name__ == "__main__":
	app.run_server(host='0.0.0.0', port=80, debug=True)
