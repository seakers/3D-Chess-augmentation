from flask import Flask, render_template, url_for, request, jsonify, redirect
from dash.dependencies import Input, Output, State
from collections import OrderedDict

import os
import sys
import time
import socket
import statistics
import json
import pprint
import redis
import pickle
import pandas               as pd
import pygmo                as pg
import plotly.graph_objs    as go
import numpy                as np

import dash
import dash_table
import dash_core_components as dcc
import dash_html_components as html
import dash_bootstrap_components as dbc

from .PlotClasses import ArchitectureInterface, MissionInterface, RedisInterface, objective_to_tokens
from app import app

cost_objectives = ["groundCost",
				   "hardwareCost",
				   "iatCost",
				   "launchCost",
				   "lifecycleCost",
				   "nonRecurringCost",
				   "operationsCost",
				   "programCost",
				   "recurringCost"]

orbital_objectives = ["MinTime",
					  "MaxTime",
					  "MinTimeToCoverage",
					  "MaxTimeToCoverage",
					  "MeanTimeToCoverage",
					  "MinAccessTime",
					  "MaxAccessTime",
					  "MeanAccessTime",
					  "MinRevisitTime",
					  "MaxRevisitTime",
					  "MeanRevisitTime",
					  "MinResponseTime",
					  "MeanResponseTime",
					  "Coverage",
					  "MinNumOfPOIpasses",
					  "MaxNumOfPOIpasses",
					  "MeanNumOfPOIpasses",
					  "MaxDataLatency",
					  "MeanDataLatency",
					  "NumGSpassesPD",
					  "TotalDownlinkTimePD",
					  "MinDownlinkTimePerPass",
					  "MaxDownlinkTimePerPass",
					  "MeanDownlinkTimePerPass"]

instrument_objectives = ["Mean of Mean of Incidence angle [deg]",
						 "SD of Mean of Incidence angle [deg]",
						 "Mean of SD of Incidence angle [deg]",
						 "Mean of Mean of Look angle [deg]",
						 "SD of Mean of Look angle [deg]",
						 "Mean of SD of Look angle [deg]",
						 "Mean of Mean of Observation Range [km]",
						 "SD of Mean of Observation Range [km]",
						 "Mean of SD of Observation Range [km]",
						 "Mean of Mean of Noise-Equivalent delta T [K]",
						 "SD of Mean of Noise-Equivalent delta T [K]",
						 "Mean of SD of Noise-Equivalent delta T [K]",
						 "Mean of Mean of DR",
						 "SD of Mean of DR",
						 "Mean of SD of DR",
						 "Mean of Mean of SNR",
						 "SD of Mean of SNR",
						 "Mean of SD of SNR",
						 "Mean of Mean of Ground Pixel Along-Track Resolution [m]",
						 "SD of Mean of Ground Pixel Along-Track Resolution [m]",
						 "Mean of SD of Ground Pixel Along-Track Resolution [m]",
						 "Mean of Mean of Ground Pixel Cross-Track Resolution [m]",
						 "SD of Mean of Ground Pixel Cross-Track Resolution [m]",
						 "Mean of SD of Ground Pixel Cross-Track Resolution [m]",
						 "Mean of Mean of Swath-Width [m]",
						 "SD of Mean of Swath-Width [m]",
						 "Mean of SD of Swath-Width [m]",
						 "Mean of Mean of Sigma NEZ Nought [dB]",
						 "SD of Mean of Sigma NEZ Nought [dB]",
						 "Mean of SD of Sigma NEZ Nought [dB]"]

value_objectives = ["Total Architecture Value [Mbits]",
                    "Total Data Collected [Mbits]",
                    "EDA Scaling Factor"]


time_objectives = ["MinTime",
                   "MaxTime",
                   "MinTimeToCoverage",
                   "MaxTimeToCoverage",
                   "MeanTimeToCoverage",
                   "MinAccessTime",
                   "MaxAccessTime",
                   "MeanAccessTime",
                   "MinRevisitTime",
                   "MaxRevisitTime",
                   "MeanRevisitTime",
                   "MinResponseTime",
                   "MeanResponseTime",
                   "MaxDataLatency",
                   "MeanDataLatency",
                   "TotalDownlinkTimePD",
                   "MinDownlinkTimePerPass",
                   "MaxDownlinkTimePerPass",
                   "MeanDownlinkTimePerPass"]  # --> All initially in seconds

data_objectives = ["Total Architecture Value [Mbits]",
                   "Total Data Collected [Mbits]"]  # --> All initially in Mbits



cost_values = ["Cost"]
interface_padding = {'padding-left': '10px', 'padding-right': '10px'}
interface_input_padding = {'padding-left': '18px'}

# REDIS INTERFACE
ri = RedisInterface("/mission")




def update_interfaces():
	arch_interfaces = []
	rootdir = '/mission'
	for subdir, dirs, files in os.walk(rootdir):
		for d in sorted(dirs):
			if (d[:4] != 'arch'):
				break
			archdir = rootdir + '/' + str(d)
			temp = ArchitectureInterface(archdir)
			if (temp.is_valid()):
				arch_interfaces.append(temp)
	return arch_interfaces


layout = html.Div(
	[
		dbc.Container(
			[

				dbc.Row([
					# Left large column
					dbc.Col([
						# Plot
						dbc.Row([
							dbc.Col([
								html.Div([
									dcc.Loading(id='overview-graph-loading', children=[dcc.Graph(id='overview-graph')],
												type="default"),
									dcc.Interval(
										id='interval-component',
										interval=120 * 1000,  # in milliseconds
										n_intervals=0
									),
								],
									className='overview-chart-layout',
								),
							],
								width=12,
								className='',
							)

						],
							no_gutters=False,
							className='',
						),

						# Table
						dbc.Row([
							dbc.Col([
								html.Div([
									dcc.Loading(children=[
										dash_table.DataTable(
											style_cell={'textAlign': 'left'},
											style_as_list_view=False,
											style_header={
												'backgroundColor': 'white',
												'fontWeight': 'bold'
											},
											id='overview-table',
											sort_action='native',
											row_selectable='multi',
										)
									], type="default")
								],
									className='overview-table-layout text-center',
								),
							],
								width=12,
								className='',
							)

						],
							no_gutters=False,
							className='',
						),

					],
						width=9,
						className='',
					),

					# Overview Chart Interface -- START
					dbc.Col([

						dbc.Row([
							dbc.Col([
								html.Div([

									# OBJECTIVES
									dbc.FormGroup(
										[
											dbc.Label("Objectives", className='interface-font-lg label-margin'),

											dbc.FormGroup(
												[
													dbc.Label("Cost", width=2, className='overview-dropdown-sublabel'),
													dbc.Col(
														dcc.Dropdown(
															options=[
																{'label': i, 'value': i} for i in cost_objectives
															],
															value=['lifecycleCost'],
															id='cost-objectives-input',
															multi=True,
															placeholder="min two total objectives...",
															className='analysis-sats-label-dropdown text-left',
														),
														width=10,
													),
												],
												row=True,
												className='specify-axis-format'
											),
											dbc.FormGroup(
												[
													dbc.Label("Orbit", width=2, className='overview-dropdown-sublabel'),
													dbc.Col(
														dcc.Dropdown(
															options=[
																{'label': i, 'value': i} for i in orbital_objectives
															],
															value=['MaxRevisitTime'],
															id='orbital-objectives-input',
															multi=True,
															placeholder="min two total objectives...",
															className='analysis-sats-label-dropdown text-left',
														),
														width=10,
													),
												],
												row=True,
												className='specify-axis-format'
											),
											dbc.FormGroup(
												[
													dbc.Label("Value", width=2, className='overview-dropdown-sublabel'),
													dbc.Col(
														dcc.Dropdown(
															options=[
																{'label': i, 'value': i} for i in value_objectives
															],
															id='value-objectives-input',
															multi=True,
															placeholder="min two total objectives...",
															className='analysis-sats-label-dropdown text-left',
														),
														width=10,
													),
												],
												row=True,
												className='specify-axis-format'
											),
										],
										className='specify-axis-format-outer interface-font-sm'
									),

									# DATA POINTS
									dbc.FormGroup(
										[
											dbc.Label("Data Points", className='interface-font-lg label-margin'),
											dcc.Dropdown(
												options=[
													{'label': 'All Architectures', 'value': 'all'},
													{'label': 'Pareto Front', 'value': 'pareto'},
												],
												value='all',
												id='overview-plot-datapoints',
												multi=False,
												clearable=False,
												className='text-left checklist-format analysis-sats-label-dropdown',
											),
										], className='data-points-format'
									),

									# FLUSH DATABASE
									dbc.FormGroup(
										[
											dbc.Button("Refresh Architectures", color="primary", className="mr-1", id='flush-database-button',
													   style={'width': '100%'}),

										],
										row=False,
										className='specify-axis-format text-left'
									),


								],
									className='overview-chart-interface-layout-1 text-center',
								),
							],
								width=12,
							)
						],
							no_gutters=False,
						),

						dbc.Row([
							dbc.Col([
								html.Div([

									# SPECIFY AXIS
									dbc.FormGroup(
										[
											dbc.Label("Specify Axis", className='interface-font-lg label-margin'),

											dbc.FormGroup(
												[
													dbc.Label("X", width=2, className='overview-dropdown-sublabel'),
													dbc.Col(
														dcc.Dropdown(
															options=[],
															value='',
															id='x-axis-dropdown',
															clearable=False,
															className='analysis-sats-label-dropdown text-left',

														),
														width=10,
													),
												],
												row=True,
												className='specify-axis-format'
											),
											dbc.FormGroup(
												[
													dbc.Label("Y", width=2, className='overview-dropdown-sublabel'),
													dbc.Col(
														dcc.Dropdown(
															options=[],
															value='',
															id='y-axis-dropdown',
															clearable=False,
															className='analysis-sats-label-dropdown text-left',
														),
														width=10,
													),
												],
												row=True,
												className='specify-axis-format'
											),
										],
										className='specify-axis-format-outer interface-font-sm'
									),

									# MARKER SPECIFICATIONS
									dbc.FormGroup(
										[
											dbc.Label("Specify Markers", className='interface-font-lg label-margin'),
											dbc.FormGroup(
												[
													dbc.Label("Color", width=2, className='overview-dropdown-sublabel'),
													dbc.Col(
														dcc.Dropdown(
															options=[],
															value='',
															id='marker-dropdown-color',
															disabled=True,
															clearable=False,
															className='analysis-sats-label-dropdown text-left',
														),
														width=10,
													),
												],
												row=True,
												className='specify-axis-format text-left'
											),
											dbc.FormGroup(
												[
													dbc.Label("Size", width=2, className='overview-dropdown-sublabel'),
													dbc.Col(
														dcc.Dropdown(
															options=[],
															value='',
															id='marker-dropdown-size',
															disabled=True,
															clearable=False,
															className='analysis-sats-label-dropdown text-left',
														),
														width=10,
													),
												],
												row=True,
												className='specify-axis-format text-left'
											),
										], className='marker-type-format'
									),

								],
									className='overview-chart-interface-layout-2 text-center',
								),
							],
								width=12,
							)
						],
							no_gutters=False,
						),

						# THIRD ROW
						dbc.Row([
							dbc.Col([
								html.Div([
									# TABLE SPECIFICATIONS
									dbc.FormGroup(
										[
											dbc.Label("Specify Table Data", className='interface-font-lg label-margin'),
											dbc.FormGroup(
												[
													dbc.Label("Data", width=2, className='overview-dropdown-sublabel'),
													dbc.Col(
														dcc.Dropdown(
															options=[{'label': i, 'value': i} for i in (
																	value_objectives + cost_objectives + orbital_objectives)],
															value=['lifecycleCost', 'MeanAccessTime', 'MaxRevisitTime',
																   'Total Architecture Value [Mbits]'],
															id='table-dropdown-data',
															disabled=False,
															clearable=True,
															placeholder="select columns...",
															multi=True,
														),
														width=10,
													),
												],
												row=True,
												className='specify-axis-format text-left'
											),
										], className='marker-type-format'
									),

								],
									className='overview-chart-interface-layout-2 text-center',
								),
							],
								width=12,
								className='',
							)
						],
							no_gutters=False,
						),

					],
						width=3,
						className='overview-chart-interface-column',
					),
					# Overview Chart Interface -- END

				],
					no_gutters=True,
					className='',
				)

			],
			fluid=True, className='overview-page-container'),
	],
)


# ------------------- CALLBACKS ------------------- #
@app.callback(  # Set the disabled and values based on objectives --- then set the options based on the values
	[
		Output('x-axis-dropdown', 'disabled'),
		Output('x-axis-dropdown', 'value'),
		Output('x-axis-dropdown', 'options'),
		Output('y-axis-dropdown', 'disabled'),
		Output('y-axis-dropdown', 'value'),
		Output('y-axis-dropdown', 'options'),
		Output('marker-dropdown-color', 'disabled'),
		Output('marker-dropdown-color', 'value'),
		Output('marker-dropdown-color', 'options'),
		Output('marker-dropdown-size', 'disabled'),
		Output('marker-dropdown-size', 'value'),
		Output('marker-dropdown-size', 'options')
	],
	[Input('cost-objectives-input', 'value'),
	 Input('orbital-objectives-input', 'value'),
	 Input('value-objectives-input', 'value'), ])
def interface_panel_options(cost_objectives, orbital_objectives, value_objectives):
	objectives = []
	if (cost_objectives is not None):
		objectives = objectives + cost_objectives
	if (orbital_objectives is not None):
		objectives = objectives + orbital_objectives
	if (value_objectives is not None):
		objectives = objectives + value_objectives

	options = []
	for i in objectives:
		options.append({'label': i, 'value': i})

	if (len(objectives) < 2):
		return True, '', [], True, '', [], True, '', [], True, '', []
	elif (len(objectives) == 2):
		return False, objectives[0], options, False, objectives[1], options, True, '', [], True, '', []
	elif (len(objectives) == 3):
		return False, objectives[0], options, False, objectives[1], options, False, objectives[2], options, True, '', []
	elif (len(objectives) > 3):
		return False, objectives[0], options, False, objectives[1], options, False, objectives[2], options, False, \
			   objectives[3], options


def is_pareto_efficient(costs):
	"""
	:param costs: An (n_points, n_costs) array
	:return: A (n_points, ) boolean array, indicating whether each point is Pareto efficient
	"""
	is_efficient = np.ones(costs.shape[0], dtype=bool)
	for i, c in enumerate(costs):
		if is_efficient[i]:
			is_efficient[is_efficient] = np.any(costs[is_efficient] < c, axis=1)  # Keep any point with a lower cost
			is_efficient[i] = True  # And keep self
	return is_efficient

def calc_pareto_front(architectures, x_data, y_data, marker_values, marker_sizes):
	datapoint_list = []
	for x in range(0, len(architectures)):
		datapoint = []
		if x_data:
			datapoint.append(x_data[x])
		if y_data:
			datapoint.append(y_data[x])
		if marker_values:
			datapoint.append(marker_values[x])
		if marker_sizes:
			datapoint.append(marker_sizes[x])
		datapoint_list.append(datapoint)
	non_dominated = is_pareto_efficient(np.array(datapoint_list))
	print(non_dominated)
	print(datapoint_list)
	for x in reversed(range(0, len(non_dominated))):
		if not non_dominated[x]:
			if x_data:
				del x_data[x]
			if y_data:
				del y_data[x]
			if marker_values:
				del marker_values[x]
			if marker_sizes:
				del marker_sizes[x]
			if architectures:
				del architectures[x]
	return architectures, x_data, y_data, marker_values, marker_sizes



def convert_value(values, objective):
	if values:
		median_value = statistics.median(values)
	else:
		median_value = 10

	divisor = 1
	return_values = []
	units = ''

	# --> Determine the divisor to use in conversion
	if objective in time_objectives:
		if median_value > 86400:
			divisor = 86400.0  # --> Days
			units = objective + ' [Days]'
		elif median_value > 3600:
			divisor = 3600.0  # --> Hours
			units = objective + ' [Hours]'
		else:
			units = objective + ' [Sec]'
	elif objective in data_objectives:
		if median_value > 1000000:
			divisor = 1000000.0  # --> Tbits
			units = objective[:-7] + '[Tbits]'
		elif median_value > 1000:
			divisor = 1000.0  # --> Gbits
			units = objective[:-7] + '[Gbits]'
		else:
			units = objective
	elif objective in cost_objectives:
		if median_value > 1000000:
			divisor = 1000000.0  # --> Trillions
			units = objective + ' [Trillions]'
		elif median_value > 1000:
			divisor = 1000.0  # --> Billions
			units = objective + ' [Billions]'
		else:
			units = objective

	for value in values:
		converted = round((float(value) / divisor), 2)
		return_values.append(converted)

	return return_values, units


database_flushes = -1

@app.callback(
	Output('overview-graph', 'figure'),
	[
		Input('x-axis-dropdown', 'value'),  # x_value
		Input('y-axis-dropdown', 'value'),  # y_value
		Input('marker-dropdown-color', 'value'),  # marker_color
		Input('marker-dropdown-size', 'value'),  # marker_size
		Input('cost-objectives-input', 'value'),  # cost_objectives
		Input('orbital-objectives-input', 'value'),  # orbital_objectives
		Input('value-objectives-input', 'value'),  # value_objectives
		Input('overview-plot-datapoints', 'value'),  # chart_type
		Input('flush-database-button', 'n_clicks'),  # flush_database
		Input('interval-component', 'n_intervals'),  # interval
	])
def render_graph(x_value, y_value, marker_color, marker_size, cost_objectives, orbital_objectives, value_objectives, chart_type, flush_database, interval):
	global database_flushes
	if database_flushes != flush_database:
		print("Flushed Database")
		ri.flush_database()
		database_flushes = flush_database


	# --> Update the database every time the user makes a request
	ri.update_database()

	# --> Calculating the time it takes for redis to retrieve the plot information
	start_time = time.time()

	# --> Get all the objectives in the plot
	objectives = []
	if cost_objectives is not None:
		objectives = objectives + cost_objectives
	if orbital_objectives is not None:
		objectives = objectives + orbital_objectives
	if value_objectives is not None:
		objectives = objectives + value_objectives

	# --> Return empty graph if we have less than two objectives
	if len(objectives) < 2:
		return {'data': []}

	# --> Here we get all the data for the plots
	architectures, x_data, y_data, marker_values, marker_sizes = ri.query_global_data(x_value, y_value, marker_color, marker_size)

	units = []
	x_title = x_value
	y_title = y_value
	marker_color_title = marker_color
	marker_size_title = marker_size
	if x_data:
		converted = convert_value(x_data, x_value)
		x_data = converted[0]
		x_units = converted[1]
		x_title = x_units
		units.append(x_units)

	if y_data:
		converted = convert_value(y_data, y_value)
		y_data = converted[0]
		y_units = converted[1]
		y_title = y_units
		units.append(y_units)

	if marker_color:
		converted = convert_value(marker_values, marker_color)
		marker_values = converted[0]
		marker_color_units = converted[1]
		marker_color_title = marker_color_units
		units.append(marker_color_units)

	if marker_size:
		converted = convert_value(marker_sizes, marker_size)
		marker_sizes = converted[0]
		marker_size_units = converted[1]
		marker_size_title = marker_size_units
		units.append(marker_size_units)



	# --> Here we will calculate the pareto front if need be
	if chart_type == 'pareto':
		architectures, x_data, y_data, marker_values, marker_sizes = calc_pareto_front(architectures, x_data, y_data, marker_values, marker_sizes)



	if len(objectives) == 2:
		return plot_two_objectives(architectures, x_data, y_data, x_title, y_title, start_time, units)
	elif len(objectives) == 3:  # Create marker_color
		return plot_three_objectives(architectures, x_data, y_data, x_title, y_title, marker_values, marker_color_title, start_time, units)
	elif len(objectives) > 3:  # Create marker_size
		return plot_multi_objectives(architectures, x_data, y_data, x_title, y_title, marker_values, marker_color_title, marker_sizes, marker_size_title, start_time, units)

global_tick_format = '.2f'

def plot_two_objectives(architectures, x_data, y_data, x_title, y_title, start_time, units):
	graph_title = str(x_title) + ' vs ' + str(y_title)
	return {
		'data': [
			go.Scattergl(
				x=x_data,
				y=y_data,
				text=architectures,
				mode='markers',
				customdata=architectures,
				marker=dict(
					size=10,
					line=dict(
						width=1,
					)
				),
			),
		],
		'layout': go.Layout(
			xaxis=dict(
				title=x_title,
				showgrid=True,
				zeroline=True,
				showline=True,
				gridcolor='#bdbdbd',
				tickformat=global_tick_format,
			),
			yaxis=dict(
				title=y_title,
				showgrid=True,
				zeroline=True,
				showline=True,
				gridcolor='#bdbdbd',
				tickformat=global_tick_format,
			),
			hovermode='closest',
			font=dict(size=13, color='#2a3f5f'),
			title=graph_title,
		)
	}


def plot_three_objectives(architectures, x_data, y_data, x_title, y_title, marker_color, marker_color_name, start_time, units):
	graph_title = str(x_title) + ' vs ' + str(y_title)
	plot_text = []
	if (len(marker_color) > 0):
		for x in range(0, len(architectures)):
			plot_text.append(
				str(architectures[x]) + '<br>' + str(marker_color_name) + ': ' + str(round(marker_color[x], 2)))
	else:
		return {'data': []}

	return {
		'data': [
			go.Scatter(
				x=x_data,
				y=y_data,
				text=plot_text,
				mode='markers',
				customdata=architectures,
				marker=dict(
					size=10,
					color=marker_color,
					colorscale='Viridis',
					showscale=True,
					colorbar=dict(title=marker_color_name),
					line=dict(width=1)
				)
			)
		],
		'layout': go.Layout(
			xaxis=dict(
				title=x_title,
				showgrid=True,
				zeroline=True,
				showline=True,
				gridcolor='#bdbdbd',
			),
			yaxis=dict(
				title=y_title,
				showgrid=True,
				zeroline=True,
				showline=True,
				gridcolor='#bdbdbd',
			),
			hovermode='closest',
			font=dict(size=13, color='#2a3f5f'),
			title=graph_title,
		)
	}


def plot_multi_objectives(architectures, x_data, y_data, x_title, y_title, marker_color, marker_color_name, marker_size, marker_size_name, start_time, units):
	graph_title = str(x_title) + ' vs ' + str(y_title)
	plot_text = []
	if (len(marker_color) > 0 and len(marker_size) > 0):
		for x in range(0, len(architectures)):
			plot_text.append(str(architectures[x]) + '<br>' + str(marker_color_name) + ': ' + str(
				round(marker_color[x], 2)) + '<br>' + str(marker_size_name) + ': ' + str(round(marker_size[x], 2)))
	else:
		return {'data': []}

	return {
		'data': [
			go.Scatter(
				x=x_data,
				y=y_data,
				text=plot_text,
				mode='markers',
				customdata=architectures,
				marker=dict(
					size=marker_size,
					sizemode='area',
					sizeref=2. * max(marker_size) / (40. ** 2),
					sizemin=4,
					color=marker_color,
					colorscale='Viridis',
					showscale=True,
					colorbar=dict(
						title=marker_color_name),
					line=dict(
						width=1,
					)
				)
			)
		],
		'layout': go.Layout(
			xaxis=dict(
				title=x_title,
				showgrid=True,
				zeroline=True,
				showline=True,
				gridcolor='#bdbdbd',
			),
			yaxis=dict(
				title=y_title,
				showgrid=True,
				zeroline=True,
				showline=True,
				gridcolor='#bdbdbd',
			),
			hovermode='closest',
			font=dict(size=13, color='#2a3f5f'),
			title=graph_title,
		)
	}




@app.callback(
	[
		Output('overview-table', 'columns'),
		Output('overview-table', 'data'),
		Output('overview-table', 'style_data_conditional'),
	],
	[
		Input('overview-graph', 'figure'),
		Input('overview-graph', 'clickData'),
		Input('overview-graph', 'selectedData'),
		Input('table-dropdown-data', 'value'),
	])
def render_table(figure, selectedData, selectedData_box, columns):  # Return columns and column data
	# --> If the current overview plot has no data
	if (figure is None):
		return [], [], []


	# GET ALL ARCHITECUTURES
	arch_interfaces = ri.arch_list

	# GET LIST OF ARCHITECTURES PLOTTED IN THE GRAPH
	arch_list = []

	try:
		arch_plotted = figure['data'][0]['customdata']
	except:
		return [], [], []

	for arch in arch_interfaces:
		if (arch in arch_plotted):
			arch_list.append(arch)


	# --> Get all data for each column, these values will be converted
	all_columns = []
	all_units = []
	for col in columns:
		tokens = objective_to_tokens(col)
		col_data = []
		for arch in arch_list:
			data = float(ri.query_arch(arch, col, *tokens))
			col_data.append(data)

		converted = convert_value(col_data, col)
		data_list = converted[0]
		units = converted[1]
		all_columns.append(data_list)
		all_units.append(units)


	# --> Get column headers with specified units
	column_data = []
	column_data.append({'id': 'Architecture', 'name': 'Architecture'})
	for col in all_units:
		column_data.append({'id': col, 'name': col})


	# --> Convert to chart data
	chart_data = []
	for x in range(len(all_columns[0])):  # --> Iterate over each arch
		row = dict()
		row['Architecture'] = arch_list[x]
		for y in range(len(all_columns)):  # --> Iterate over each value per arch
			row[all_units[y]] = all_columns[y][x]
		chart_data.append(row)





	# STYLE HIGHLIGHTED ARCHITECTURES
	styled_data = style_data_conditional(selectedData, selectedData_box)
	return column_data, chart_data, styled_data


def style_data_conditional(selectedData, selectedData_box):
	# FOR NOW WE WILL ONLY DO SINGLE CLICK HIGHLIGHTING
	styled_data = []

	if (selectedData is None):
		return styled_data

	if (selectedData is not None):  # SELECT ONE POINT
		selected_arch_num = selectedData['points'][0]['pointIndex']
		styled_data = [{
			"if": {"row_index": selected_arch_num},
			"backgroundColor": "#2a3f5f",
			'color': 'white'
		}]
	# if(selectedData_box is not None and selectedData is None): # BOX SELECT POINTS
	# 	selected_points = selectedData_box['points']
	# 	for point in selected_points:
	# 		selected_arch_num = point['pointIndex']
	# 		styled_data.append({
	# 				        "if": {"row_index": selected_arch_num},
	# 				        "backgroundColor": "#3D9970",
	# 				        'color': 'white'
	# 				    })
	return styled_data
